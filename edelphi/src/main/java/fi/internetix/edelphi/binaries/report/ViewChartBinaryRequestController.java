package fi.internetix.edelphi.binaries.report;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;

import fi.internetix.edelphi.EdelfoiStatusCode;
import fi.internetix.edelphi.binaries.BinaryController;
import fi.internetix.edelphi.dao.panels.PanelStampDAO;
import fi.internetix.edelphi.dao.querylayout.QueryPageDAO;
import fi.internetix.edelphi.domainmodel.panels.PanelStamp;
import fi.internetix.edelphi.domainmodel.querylayout.QueryPage;
import fi.internetix.edelphi.pages.panel.admin.report.util.ChartModelProvider;
import fi.internetix.edelphi.pages.panel.admin.report.util.QueryReportChartContext;
import fi.internetix.edelphi.pages.panel.admin.report.util.QueryReportPageController;
import fi.internetix.edelphi.pages.panel.admin.report.util.QueryReportPageProvider;
import fi.internetix.edelphi.taglib.chartutil.ChartWebHelper;
import fi.internetix.smvc.SmvcRuntimeException;
import fi.internetix.smvc.controllers.BinaryRequestContext;

public class ViewChartBinaryRequestController extends BinaryController {

  @Override
  public void process(BinaryRequestContext binaryRequestContext) {
//    Double width = parseDimension(binaryRequestContext.getDouble("width"), 600);
//    Double height = parseDimension(binaryRequestContext.getDouble("height"), 400);
//    String outputFormat = parseRenderType(binaryRequestContext.getString("render"));
//    Long queryPageId = binaryRequestContext.getLong("queryPageId");
//    Long stampId = binaryRequestContext.getLong("stampId");
//    
//    try {
//      if (!ChartWebHelper.checkOutputType(outputFormat)) {
//        throw new SmvcRuntimeException(EdelfoiStatusCode.REPORTING_ERROR, "Unknown output format.");
//      }
//
//      QueryPageDAO queryPageDAO = new QueryPageDAO();
//      QueryPage queryPage = queryPageDAO.findById(queryPageId);
//      
//      PanelStampDAO panelStampDAO = new PanelStampDAO();
//      PanelStamp panelStamp = panelStampDAO.findById(stampId);
//      
//      QueryReportPageController queryReportPageController = QueryReportPageProvider.getController(queryPage.getPageType());
//      QueryReportChartContext queryReportChartContext = new QueryReportChartContext(binaryRequestContext.getRequest().getLocale(), panelStamp);
//      queryReportChartContext.populateRequestParameters(binaryRequestContext);
//      Chart chartModel = queryReportPageController.constructChart(queryReportChartContext, queryPage);
//      
//      if (chartModel != null) {
//        // Set size in chart model
//        Bounds bounds = chartModel.getBlock().getBounds();
//        bounds.setWidth(width);
//        bounds.setHeight(height);
//      }
//      else {
//        throw new SmvcRuntimeException(EdelfoiStatusCode.REPORTING_ERROR, "ChartModel was not found.");
//      }
//
//      binaryRequestContext.setResponseContent(ChartModelProvider.getChartData(chartModel, outputFormat), ChartModelProvider.getContentType(outputFormat));
//    }
//    catch (Exception e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
  }

//  private String parseRenderType(String renderType) {
//    if ("png".equalsIgnoreCase(renderType))
//      return "PNG";
//
//    if ("svg".equalsIgnoreCase(renderType))
//      return "SVG";
//    
//    return "PNG";
//  }
//
//  private double parseDimension(Double dimension, double def) {
//    if (dimension != null)
//      return dimension.doubleValue();
//    else
//      return def;
//  }
}
