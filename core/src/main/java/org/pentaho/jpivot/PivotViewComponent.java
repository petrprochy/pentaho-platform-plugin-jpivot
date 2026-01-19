/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.jpivot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.jpivot.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class PivotViewComponent extends ComponentBase {

  public static final String MODE = "mode"; //$NON-NLS-1$

  public static final String MODEL = "model"; //$NON-NLS-1$

  public static final String OPTIONS = "options"; //$NON-NLS-1$

  public static final String CONNECTION = "connection"; //$NON-NLS-1$

  public static final String TITLE = "title"; //$NON-NLS-1$

  public static final String URL = "url"; //$NON-NLS-1$

  public static final String VIEWER = "viewer"; //$NON-NLS-1$

  public static final String EXECUTE = "execute"; //$NON-NLS-1$

  public static final String SHOWGRID = "showgrid"; //$NON-NLS-1$

  public static final String LEVEL_STYLE = "levelstyle"; //$NON-NLS-1$

  public static final String HIDE_SPANS = "hidespans"; //$NON-NLS-1$

  public static final String SHOW_PROPERTIES = "showproperties"; //$NON-NLS-1$

  public static final String CHARTTYPE = "charttype"; //$NON-NLS-1$

  public static final String CHARTLOCATION = "chartlocation"; //$NON-NLS-1$

  public static final String CHARTWIDTH = "chartwidth"; //$NON-NLS-1$

  public static final String CHARTHEIGHT = "chartheight"; //$NON-NLS-1$

  public static final String CHARTDRILLTHROUGHENABLED = "chartdrillthroughenabled"; //$NON-NLS-1$

  public static final String CHARTTITLE = "charttitle"; //$NON-NLS-1$

  public static final String CHARTTITLEFONTFAMILY = "charttitlefontfamily"; //$NON-NLS-1$

  public static final String CHARTTITLEFONTSTYLE = "charttitlefontstyle"; //$NON-NLS-1$

  public static final String CHARTTITLEFONTSIZE = "charttitlefontsize"; //$NON-NLS-1$

  public static final String CHARTHORIZAXISLABEL = "charthorizaxislabel"; //$NON-NLS-1$

  public static final String CHARTVERTAXISLABEL = "chartvertaxislabel"; //$NON-NLS-1$

  public static final String CHARTAXISLABELFONTFAMILY = "chartaxislabelfontfamily"; //$NON-NLS-1$

  public static final String CHARTAXISLABELFONTSTYLE = "chartaxislabelfontstyle"; //$NON-NLS-1$

  public static final String CHARTAXISLABELFONTSIZE = "chartaxislabelfontsize"; //$NON-NLS-1$

  public static final String CHARTAXISTICKFONTFAMILY = "chartaxistickfontfamily"; //$NON-NLS-1$

  public static final String CHARTAXISTICKFONTSTYLE = "chartaxistickfontstyle"; //$NON-NLS-1$

  public static final String CHARTAXISTICKFONTSIZE = "chartaxistickfontsize"; //$NON-NLS-1$

  public static final String CHARTAXISTICKLABELROTATION = "chartaxisticklabelrotation"; //$NON-NLS-1$

  public static final String CHARTSHOWLEGEND = "chartshowlegend"; //$NON-NLS-1$

  public static final String CHARTLEGENDLOCATION = "chartlegendlocation"; //$NON-NLS-1$

  public static final String CHARTLEGENDFONTFAMILY = "chartlegendfontfamily"; //$NON-NLS-1$

  public static final String CHARTLEGENDFONTSTYLE = "chartlegendfontstyle"; //$NON-NLS-1$

  public static final String CHARTLEGENDFONTSIZE = "chartlegendfontsize"; //$NON-NLS-1$

  public static final String CHARTSHOWSLICER = "chartshowslicer"; //$NON-NLS-1$

  public static final String CHARTSLICERLOCATION = "chartslicerlocation"; //$NON-NLS-1$

  public static final String CHARTSLICERALIGNMENT = "chartsliceralignment"; //$NON-NLS-1$

  public static final String CHARTSLICERFONTFAMILY = "chartslicerfontfamily"; //$NON-NLS-1$

  public static final String CHARTSLICERFONTSTYLE = "chartslicerfontstyle"; //$NON-NLS-1$

  public static final String CHARTSLICERFONTSIZE = "chartslicerfontsize"; //$NON-NLS-1$

  public static final String CHARTBACKGROUNDR = "chartbackgroundr"; //$NON-NLS-1$

  public static final String CHARTBACKGROUNDG = "chartbackgroundg"; //$NON-NLS-1$

  public static final String CHARTBACKGROUNDB = "chartbackgroundb"; //$NON-NLS-1$

  public static final String ROLE = "role"; //$NON-NLS-1$

  public static final String CUBE = "cube"; //$NON-NLS-1$

  private static final long serialVersionUID = -327755990995067478L;

  private static final Collection<String> ignoreInputs = Arrays.asList(
      PivotViewComponent.MODE,
      StandardSettings.SQL_QUERY,
      StandardSettings.QUERY_NAME,
      PivotViewComponent.VIEWER
  );

  @Override
  public Log getLogger() {
    return LogFactory.getLog(PivotViewComponent.class);
  }

  @Override
  protected boolean validateAction() {

    if (!isDefinedOutput(PivotViewComponent.OPTIONS)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0001_OPTIONS_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.MODEL)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0002_MODEL_NOT_DEFIEND")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.CONNECTION)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0003_CONNECTION_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(StandardSettings.MDX_QUERY)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0004_MDX_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.TITLE)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0007_TITLE_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedInput(PivotViewComponent.MODE)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0005_MODE_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.URL)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0008_URL_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedInput(StandardSettings.SQL_QUERY) && !isDefinedInput(StandardSettings.QUERY_NAME)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0009_QUERY_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }

    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() throws Throwable {

    @SuppressWarnings("unchecked") Set<String> inputNames = getInputNames();
    @SuppressWarnings("unchecked") Set<String> outputNames = getOutputNames();

    String mode = getInputStringValue(PivotViewComponent.MODE);
    if (!mode.equals(PivotViewComponent.EXECUTE)) {
      // assume this is a redirect
      if (!isDefinedOutput(PivotViewComponent.URL)) {
        // we need the viewer output
        error(Messages.getInstance().getString("PivotView.ERROR_0006_VIEWER_NOT_DEFINED")); //$NON-NLS-1$
        return false;
      }
      final StringBuilder viewer = new StringBuilder(getInputStringValue(PivotViewComponent.VIEWER));
      if (viewer.toString().indexOf('?') == -1) {
        viewer.append("?solution=").append(getSolutionName()).append("&path=").append(encode(getSolutionPath())).append("&action=")
            .append(encode(getActionName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else {
        viewer.append("solution=").append(getSolutionName()).append("&path=").append(encode(getSolutionPath())).append("&action=")
            .append(encode(getActionName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }

      for (String name : inputNames) {
        if (!PivotViewComponent.ignoreInputs.contains(name)) {
          viewer.append("&").append(name).append("=").append(encode(getInputStringValue(name))); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }

      setOutputValue(PivotViewComponent.URL, PentahoRequestContextHolder.getRequestContext().getContextPath() + viewer);
      return true;
    }

    String roleName = null;
    if (isDefinedInput(PivotViewComponent.ROLE)) {
      roleName = getInputStringValue(PivotViewComponent.ROLE);
      if (isDefinedOutput(PivotViewComponent.ROLE)) {
        setOutputValue(PivotViewComponent.ROLE, roleName);
      }
    }

    // process the model
    String model = getInputStringValue(StandardSettings.DATA_MODEL);
    if (!model.startsWith("solution:") && !model.startsWith("http:") && !model.startsWith("mondrian:")) { //$NON-NLS-1$ //$NON-NLS-2$
      model = "solution:" + model; //$NON-NLS-1$
    }

    setOutputValue(StandardSettings.DATA_MODEL, model);

    setOutputValue(CHARTTYPE);
    setOutputValue(SHOWGRID);
    setOutputValue(CHARTLOCATION);
    setOutputValue(CHARTWIDTH);
    setOutputValue(CHARTHEIGHT);
    setOutputValue(CHARTDRILLTHROUGHENABLED);
    setOutputValue(CHARTTITLE);
    setOutputValue(CHARTTITLEFONTFAMILY);
    setOutputValue(CHARTTITLEFONTSTYLE);
    setOutputValue(CHARTTITLEFONTSIZE);
    setOutputValue(CHARTHORIZAXISLABEL);
    setOutputValue(CHARTVERTAXISLABEL);
    setOutputValue(CHARTAXISLABELFONTFAMILY);
    setOutputValue(CHARTAXISLABELFONTSTYLE);
    setOutputValue(CHARTAXISLABELFONTSIZE);
    setOutputValue(CHARTAXISTICKFONTFAMILY);
    setOutputValue(CHARTAXISTICKFONTSTYLE);
    setOutputValue(CHARTAXISTICKFONTSIZE);
    setOutputValue(CHARTAXISTICKLABELROTATION);
    setOutputValue(CHARTSHOWLEGEND);
    setOutputValue(CHARTLEGENDLOCATION);
    setOutputValue(CHARTLEGENDFONTFAMILY);
    setOutputValue(CHARTLEGENDFONTSTYLE);
    setOutputValue(CHARTLEGENDFONTSIZE);
    setOutputValue(CHARTSHOWSLICER);
    setOutputValue(CHARTSLICERLOCATION);
    setOutputValue(CHARTSLICERALIGNMENT);
    setOutputValue(CHARTSLICERFONTFAMILY);
    setOutputValue(CHARTSLICERFONTSTYLE);
    setOutputValue(CHARTSLICERFONTSIZE);
    setOutputValue( CHARTBACKGROUNDR);
    setOutputValue(CHARTBACKGROUNDG);
    setOutputValue(CHARTBACKGROUNDB);

    setOutputValue(LEVEL_STYLE);
    setOutputValue(HIDE_SPANS);
    setOutputValue(SHOW_PROPERTIES);

    // process the data source connection
    String dataSource = getInputStringValue(StandardSettings.JNDI);
    setOutputValue(StandardSettings.CONNECTION, dataSource); 

    // process the query
    String queryName = StandardSettings.SQL_QUERY;
    if (inputNames.contains(StandardSettings.QUERY_NAME)) {
      queryName = getInputStringValue(StandardSettings.QUERY_NAME);
    }
    String query = getInputStringValue(queryName);

    // if query = "default", generate a query
    if (query.equals(StandardSettings.DEFAULT)) {
      // get the default cube.  This is only useful if the schema contains more
      final String cube = getInputStringValue(PivotViewComponent.CUBE);
      // we need to generate a query.
      final MondrianModelComponent mc = new MondrianModelComponent();
      final String m = model;
      final String r = roleName;
      query = Optional.ofNullable(mc.resolveQuery(m, dataSource, cube, r)).
          orElseGet(() -> mc.fallbackResolveQuery(m, dataSource, cube, r));

      if (query == null) {
        error(Messages.getInstance().getErrorString("PivotView.ERROR_0010_QUERY_GENERATION_FAILED")); //$NON-NLS-1$
        return false;
      }
    }

    String mdx = applyInputsToFormat(query);
    setOutputValue(StandardSettings.MDX_QUERY, mdx);

    String title = getInputStringValue(PivotViewComponent.TITLE);
    setOutputValue(PivotViewComponent.TITLE, title);

    // now process the options
    ArrayList<String> options = new ArrayList<>();
    Element optionsNode = (Element) getComponentDefinition().selectSingleNode("options"); //$NON-NLS-1$
    List<Element> optionNodes = optionsNode.elements();
    for (Element optionNode : optionNodes) {
      options.add(optionNode.getName());
    }
    if (!options.isEmpty()) {
      if (outputNames.contains(PivotViewComponent.OPTIONS)) {
        setOutputValue(PivotViewComponent.OPTIONS, options);
      } else {
        error(Messages.getInstance().getErrorString("PivotView.ERROR_0001_OPTIONS_NOT_DEFINED")); //$NON-NLS-1$
        return false;
      }
    }
    return true;
  }

  private void setOutputValue(String property) {
    if (isDefinedOutput(property)) {
      if (isDefinedInput(property)) {
        setOutputValue(property, getInputStringValue(property));
      }
    }
  }

  @Override
  public boolean init() {
    return true;
  }

  private static String encode(String value) throws UnsupportedEncodingException {
    return URLEncoder.encode(value, LocaleHelper.getSystemEncoding());
  }
}
