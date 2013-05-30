package fi.internetix.edelphi.binaries.queries;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import fi.internetix.edelphi.EdelfoiStatusCode;
import fi.internetix.edelphi.binaries.BinaryController;
import fi.internetix.edelphi.dao.panels.PanelStampDAO;
import fi.internetix.edelphi.dao.resources.QueryDAO;
import fi.internetix.edelphi.domainmodel.panels.PanelStamp;
import fi.internetix.edelphi.domainmodel.resources.Query;
import fi.internetix.edelphi.utils.GoogleDriveUtils;
import fi.internetix.edelphi.utils.ReportUtils;
import fi.internetix.edelphi.utils.RequestUtils;
import fi.internetix.edelphi.utils.StreamUtils;
import fi.internetix.edelphi.utils.SystemUtils;
import fi.internetix.smvc.SmvcRuntimeException;
import fi.internetix.smvc.controllers.BinaryRequestContext;
import fi.internetix.smvc.controllers.RequestContext;

public class ExportReportBinaryController extends BinaryController {

  @Override
  public void process(BinaryRequestContext requestContext) {
    Long queryId = requestContext.getLong("queryId");
    Long stampId = requestContext.getLong("stampId");

    QueryDAO queryDAO = new QueryDAO();
    PanelStampDAO panelStampDAO = new PanelStampDAO();
    Query query = queryDAO.findById(queryId);
    PanelStamp panelStamp = panelStampDAO.findById(stampId);
    ExportFormat format = ExportFormat.valueOf(requestContext.getString("format"));

    switch (format) {
    case GOOGLE_DOCUMENT:
      exportGoogleDocument(requestContext, query, panelStamp, false);
      break;
    case GOOGLE_IMAGES:
      exportGoogleDocument(requestContext, query, panelStamp, true);
      break;
    case PDF:
      exportPdf(requestContext, query, panelStamp);
      break;
    case PNG_ZIP:
      exportChartZip(requestContext, "PNG", query, panelStamp);
      break;
    case SVG_ZIP:
      exportChartZip(requestContext, "SVG", query, panelStamp);
      break;
    }
  }

  public void exportGoogleDocument(RequestContext requestContext, Query query, PanelStamp panelStamp, boolean imagesOnly) {
    Drive drive = GoogleDriveUtils.getAuthenticatedService(requestContext);
    if (drive != null) {
      try {
        String baseUrl = RequestUtils.getBaseUrl(requestContext.getRequest());
        URL url = new URL(baseUrl + "/panel/admin/report/query.page?chartFormat=SVG&queryId=" + query.getId() + "&panelId=" + panelStamp.getPanel().getId());
        File file = ReportUtils.uploadReportToGoogleDrive(requestContext.getRequest().getLocale(), drive, url, query.getName(), 3, imagesOnly);
        requestContext.setRedirectURL(file.getAlternateLink());
      }
      catch (IOException e) {
        throw new SmvcRuntimeException(EdelfoiStatusCode.REPORT_GOOGLE_DRIVE_EXPORT_FAILED, "exception.1031.reportGoogleDriveExportFailed", e);
      }
      catch (ParserConfigurationException e) {
        throw new SmvcRuntimeException(EdelfoiStatusCode.REPORT_GOOGLE_DRIVE_EXPORT_FAILED, "exception.1031.reportGoogleDriveExportFailed", e);
      }
      catch (SAXException e) {
        throw new SmvcRuntimeException(EdelfoiStatusCode.REPORT_GOOGLE_DRIVE_EXPORT_FAILED, "exception.1031.reportGoogleDriveExportFailed", e);
      }
      catch (TransformerException e) {
        throw new SmvcRuntimeException(EdelfoiStatusCode.REPORT_GOOGLE_DRIVE_EXPORT_FAILED, "exception.1031.reportGoogleDriveExportFailed", e);
      }
    }
  }

  public void exportPdf(BinaryRequestContext requestContext, Query query, PanelStamp panelStamp) {
    try {
      String baseURL = RequestUtils.getBaseUrl(requestContext.getRequest());

      URL url = new URL(baseURL + "/panel/admin/report/query.page?chartFormat=PNG&queryId=" + query.getId() + "&panelId=" + panelStamp.getPanel().getId());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty("Authorization", "InternalAuthorization " + SystemUtils.getSettingValue("system.internalAuthorizationHash"));
      connection.setRequestMethod("GET");
      connection.setReadTimeout(15 * 1000);
      connection.connect();

      String reportHtml = StreamUtils.readStreamToString(connection.getInputStream(), "UTF-8");
      ByteArrayOutputStream tidyXHtml = new ByteArrayOutputStream();
      Tidy tidy = new Tidy();
      tidy.setInputEncoding("UTF-8");
      tidy.setOutputEncoding("UTF-8");
      tidy.setShowWarnings(true);
      tidy.setNumEntities(false);
      tidy.setXmlOut(true);
      tidy.setXHTML(true);
      tidy.parse(new StringReader(reportHtml), tidyXHtml);

      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false);
      builderFactory.setValidating(false);
      builderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
      builderFactory.setFeature("http://xml.org/sax/features/validation", false);
      builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(tidyXHtml.toByteArray());
      Document doc = builder.parse(inputStream);
      ITextRenderer renderer = new ITextRenderer();

      renderer.setDocument(doc, baseURL + "/panel/admin/report/query/");
      renderer.layout();

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      renderer.createPDF(outputStream);
      outputStream.close();

      requestContext.setResponseContent(outputStream.toByteArray(), "application/pdf");
      requestContext.setFileName(query.getUrlName() + ".pdf");

    }
    catch (Exception e) {
      // TODO: Proper error handling
      e.printStackTrace();
    }
  }

  public void exportChartZip(BinaryRequestContext requestContext, String imageFormat, Query query, PanelStamp panelStamp) {
    try {
      String baseURL = RequestUtils.getBaseUrl(requestContext.getRequest());
      URL url = new URL(baseURL + "/panel/admin/report/query.page?chartFormat=" + imageFormat + "&queryId=" + query.getId() + "&panelId="
            + panelStamp.getPanel().getId());
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      try {
        ReportUtils.zipCharts(outputStream, imageFormat, url);
      }
      finally {
        outputStream.close();
      }
      requestContext.setResponseContent(outputStream.toByteArray(), "application/zip");
      requestContext.setFileName(query.getUrlName() + ".zip");
    }
    catch (Exception e) {
      // TODO: Proper error handling
      e.printStackTrace();
    }
  }

  private enum ExportFormat {
    PDF, GOOGLE_DOCUMENT, PNG_ZIP, SVG_ZIP, GOOGLE_IMAGES
  }
}