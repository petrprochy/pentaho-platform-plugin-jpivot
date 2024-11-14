package org.pentaho.jpivot;

import mondrian.olap.*;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.rolap.RolapCube;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.jpivot.proxies.ProxyServletContext;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogComplementInfo;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.util.logging.Logger;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static mondrian.rolap.RolapConnectionProperties.Catalog;

/** @since 9.4.0.0-3 */
public class MondrianModelComponent {

  public String resolveQuery(String model, String jndi, String cube, String role) {
    final Connection connection = getConnection(model, jndi, role);
    if (connection == null) return null;
    try {
      return resolveQuery(connection, cube);
    } finally {
      connection.close();
    }
  }

  public String resolveQuery(Connection connection, String cubeName) {

    final Schema schema = connection.getSchema();
    if (schema == null) {
      Logger.error("MondrianModelComponent", Messages.getInstance().getErrorString("MondrianModel.ERROR_0002_INVALID_SCHEMA", connection.getConnectString())
      );
      return null;
    }
    final RolapCube cube = (RolapCube) schema.lookupCube(cubeName, false);
    if (cube == null) {
      Logger.error("MondrianModelComponent",
          Messages.getInstance().getErrorString("MondrianModel.ERROR_0005_CUBE_NOT_FOUND", cubeName, connection.getConnectString())
      );
      return null;
    }

    String whereMdx = "";
    final String catalog = connection.getCatalogName();
    final MondrianCatalogComplementInfo catalogComplementInfo = PentahoSystem.get(MondrianCatalogHelper.class, "IMondrianCatalogService",
        PentahoSessionHolder.getSession()).getCatalogComplementInfoMap(catalog);
    Optional<String> whereCond = Optional.ofNullable(catalogComplementInfo).map(i -> i.getWhereCondition(cube.getName()));
    if (whereCond.isPresent() && !whereCond.get().isEmpty()) {
      // Caveat - It's possible that we have in the where condition a hierarchy that we don't have access
      // permissions; In this case, we'll ditch the where condition at all. Same for any error that
      // we find here
      try {
        // According to Julian, the better way to resolve the names is to build a query
        final String queryStr = "select " + whereCond.get() + " on columns, {} on rows from " + cube.getUniqueName(); //$NON-NLS-1$ //$NON-NLS-2$
        final Query query = connection.parseQuery(queryStr);
        final Hierarchy[] hierarchies = query.getMdxHierarchiesOnAxis(AxisOrdinal.StandardAxisOrdinal.COLUMNS);
        if (Arrays.stream(hierarchies).allMatch(h -> connection.getRole().canAccess(h))) {
          whereMdx = "where " + whereCond.get(); //$NON-NLS-1$
        }
      } catch (Exception ignored) {
      }
    }

    final Dimension measures = Arrays.stream(cube.getDimensions()).filter(Dimension::isMeasures).findFirst().orElse(null);
    if (measures == null) {
      Logger.error("MondrianModelComponent", Messages.getInstance().getErrorString("ERROR_0006_NO_DIMENSIONS", cube.getUniqueName(), "[Measures]"));
      return null;
    }

    final Level level = TimeLevel.fromConfig().getLevel(cube);
    final Member measure = cube.isVirtual() ? findFirstVisible(measures.getHierarchy()) : getDefaultOrFirstVisible(measures.getHierarchy());
    return "select NON EMPTY {" + measure + "} ON COLUMNS,\n" +
        "  NON EMPTY ClosingPeriod(" + level.getUniqueName() + ").Children ON ROWS\n" +
        "from " + cube.getUniqueName() +
        "\n" + whereMdx;
  }

  public String fallbackResolveQuery(String model, String jndi, String cube, String role) {
    final Connection connection = getConnection(model, jndi, role);
    try {
      assert connection != null;
      return org.pentaho.platform.plugin.action.mondrian.MondrianModelComponent.getInitialQuery(connection, cube);
    } catch (Throwable e) {
      return null;
    } finally {
      if (connection != null) connection.close();
    }
  }


  private static Member findFirstVisible(Hierarchy hierarchy) {
    return hierarchy.getDimension().getSchema().getSchemaReader().getLevelMembers(hierarchy.getDefaultMember().getLevel(), true).
        stream().filter(MondrianModelComponent::isVisible).
        findFirst().orElse(hierarchy.getDefaultMember());
  }

  private static boolean isVisible(Member member) {
    return Boolean.TRUE.equals(member.getPropertyValue(Property.VISIBLE.getName()));
  }

  private static Member getDefaultOrFirstVisible(Hierarchy hierarchy) {
    final Member m = hierarchy.getDefaultMember();
    return isVisible(m) ? m : findFirstVisible(hierarchy);
  }

  private Optional<MondrianCatalog> getCatalog(String definition) {
    IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, PentahoSessionHolder.getSession());
    return mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true).stream().
        filter(c -> c.getDefinition().equals(definition)).findFirst();
  }

  private Connection getConnection(String model, String jndi, String role) {
    final Properties properties = new Properties();
    properties.put(RolapConnectionProperties.Provider.name(), "mondrian");
    properties.put(RolapConnectionProperties.PoolNeeded.name(), "false");
    properties.put(RolapConnectionProperties.DataSource.name(), jndi);

    if (!model.startsWith("solution:") && !model.startsWith("mondrian:")) model = "mondrian:" + model;
    properties.put(Catalog.name(), model);

    if (role != null) properties.put(RolapConnectionProperties.Role.name(), role);

    getCatalog(model).ifPresent(c ->
        c.getConnectProperties().forEach(p -> properties.putIfAbsent(p.getKey(), p.getValue()))
    );

    MDXConnection mdxConnection = (MDXConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.MDX_DATASOURCE, properties,
        PentahoSessionHolder.getSession(), null);
    Connection connection = mdxConnection.getConnection();
    if (connection == null) {
      Logger.error("MondrianModelComponent",
          Messages.getInstance().getErrorString("MondrianModel.ERROR_0001_INVALID_CONNECTION", properties.toString())
      );
      return null;
    }
    return connection;
  }

  enum TimeLevel {
    ALL {
      @Override
      Level getLevel(Cube cube) {
        final Level year = YEAR.getLevel(cube);
        final Member allMember = year.getHierarchy().getAllMember();
        return allMember != null ? allMember.getLevel() : year;
      }
    },
    YEAR {
      @Override
      Level getLevel(Cube cube) {
        return cube.getYearLevel();
      }
    },
    QUARTER {
      @Override
      Level getLevel(Cube cube) {
        return cube.getQuarterLevel();
      }
    },
    MONTH {
      @Override
      Level getLevel(Cube cube) {
        return cube.getMonthLevel();
      }
    },
    ;

    Level getLevel(Cube cube) {
      throw new UnsupportedOperationException();
    }

    static TimeLevel fromConfig() {
      final String v = PentahoSystem.getSystemSetting(AnalysisViewService.SETTINGS_FILE, "new-view-level", "YEAR");
      try {
        return valueOf(v.toUpperCase());
      } catch (IllegalArgumentException e) {
        return YEAR;
      }
    }
  }
}
