package fi.internetix.edelphi.jsons.panel.admin;

import java.util.List;
import java.util.Locale;

import fi.internetix.edelphi.DelfoiActionName;
import fi.internetix.edelphi.EdelfoiStatusCode;
import fi.internetix.edelphi.dao.panels.PanelExpertiseGroupUserDAO;
import fi.internetix.edelphi.dao.panels.PanelUserExpertiseClassDAO;
import fi.internetix.edelphi.dao.panels.PanelUserExpertiseGroupDAO;
import fi.internetix.edelphi.dao.querydata.QueryQuestionAnswerDAO;
import fi.internetix.edelphi.dao.querylayout.QueryPageDAO;
import fi.internetix.edelphi.dao.querymeta.QueryFieldDAO;
import fi.internetix.edelphi.domainmodel.actions.DelfoiActionScope;
import fi.internetix.edelphi.domainmodel.panels.Panel;
import fi.internetix.edelphi.domainmodel.panels.PanelExpertiseGroupUser;
import fi.internetix.edelphi.domainmodel.panels.PanelUserExpertiseClass;
import fi.internetix.edelphi.domainmodel.panels.PanelUserExpertiseGroup;
import fi.internetix.edelphi.domainmodel.querylayout.QueryPage;
import fi.internetix.edelphi.domainmodel.querylayout.QueryPageType;
import fi.internetix.edelphi.domainmodel.querymeta.QueryOptionField;
import fi.internetix.edelphi.i18n.Messages;
import fi.internetix.edelphi.jsons.JSONController;
import fi.internetix.edelphi.query.QueryPageHandlerFactory;
import fi.internetix.edelphi.query.expertise.ExpertiseQueryPageHandler;
import fi.internetix.edelphi.utils.RequestUtils;
import fi.internetix.smvc.PageNotFoundException;
import fi.internetix.smvc.SmvcRuntimeException;
import fi.internetix.smvc.controllers.JSONRequestContext;

public class DeletePanelExpertiseClassJSONRequestController extends JSONController {

  public DeletePanelExpertiseClassJSONRequestController() {
    super();
    setAccessAction(DelfoiActionName.MANAGE_PANEL, DelfoiActionScope.PANEL);
  }

  @Override
  public void process(JSONRequestContext jsonRequestContext) {
    Long expertiseClassId = jsonRequestContext.getLong("expertiseClassId");
    PanelUserExpertiseClassDAO expertiseClassDAO = new PanelUserExpertiseClassDAO();
    PanelUserExpertiseGroupDAO expertiseGroupDAO = new PanelUserExpertiseGroupDAO();
    PanelExpertiseGroupUserDAO groupUserDAO = new PanelExpertiseGroupUserDAO();
    QueryPageDAO queryPageDAO = new QueryPageDAO();

    Panel panel = RequestUtils.getPanel(jsonRequestContext);
    if (panel == null) {
      throw new PageNotFoundException(jsonRequestContext.getRequest().getLocale());
    }

    Messages messages = Messages.getInstance();
    Locale locale = jsonRequestContext.getRequest().getLocale();

    PanelUserExpertiseClass expertiseClass = expertiseClassDAO.findById(expertiseClassId);
    if (hasAnswers(expertiseClass)) {
      throw new SmvcRuntimeException(EdelfoiStatusCode.EXPERTISE_CONTAINS_ANSWERS, messages.getText(locale, "exception.1024.expertiseContainsAnswers"));
    }
    
    List<PanelUserExpertiseGroup> groups = expertiseGroupDAO.listByExpertiseAndStamp(expertiseClass, panel.getCurrentStamp());
    
    for (PanelUserExpertiseGroup group : groups) {
      List<PanelExpertiseGroupUser> users = groupUserDAO.listByGroupAndArchived(group, Boolean.FALSE);
      
      for (PanelExpertiseGroupUser user : users) {
        groupUserDAO.delete(user);
      }
      
      expertiseGroupDAO.delete(group);
    }
    
    expertiseClassDAO.delete(expertiseClass);
    
    List<QueryPage> expertisePages = queryPageDAO.listByQueryParentFolderAndPageType(expertiseClass.getPanel().getRootFolder(), QueryPageType.EXPERTISE);
    for (QueryPage expertisePage : expertisePages) {
      ExpertiseQueryPageHandler pageHandler = (ExpertiseQueryPageHandler) QueryPageHandlerFactory.getInstance().buildPageHandler(QueryPageType.EXPERTISE);
      pageHandler.synchronizedFields(expertisePage);
    }
  }
  
  private boolean hasAnswers(PanelUserExpertiseClass expertiseClass) {
    QueryPageDAO queryPageDAO = new QueryPageDAO();
    QueryFieldDAO queryFieldDAO = new QueryFieldDAO();
    QueryQuestionAnswerDAO queryQuestionAnswerDAO = new QueryQuestionAnswerDAO();
    
    List<QueryPage> expertisePages = queryPageDAO.listByQueryParentFolderAndPageType(expertiseClass.getPanel().getRootFolder(), QueryPageType.EXPERTISE);
    for (QueryPage expertisePage : expertisePages) {
      QueryOptionField queryField = (QueryOptionField) queryFieldDAO.findByQueryPageAndName(expertisePage, getFieldName(expertiseClass));
      Long replies = queryQuestionAnswerDAO.countByQueryField(queryField);
      return replies > 0;
    }
    
    return false;
  }
  
  private String getFieldName(PanelUserExpertiseClass expertiseClass) {
    return "expertise." + expertiseClass.getId();
  }
}
