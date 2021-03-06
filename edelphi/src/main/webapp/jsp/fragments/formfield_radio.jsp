<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:set var="classes">formField formRadio</c:set>
<c:choose>
  <c:when test="${!empty(param.classes)}">
    <c:set var="classes">${classes} ${param.classes}</c:set>
  </c:when>
</c:choose>

<div class="formFieldContainer formSelectContainer">
  <c:choose>
    <c:when test="${param.labelLocale != null}">
      <label class="formFieldLabel" for="${param.id}"><fmt:message key="${param.labelLocale}"/></label>
    </c:when>
    <c:otherwise>
      <label class="formFieldLabel" for="${param.id}">${param.labelText}</label>
    </c:otherwise>
  </c:choose>
  <c:forEach var="_option" items="${param.options}">
    <c:set var="_checked">
      <c:if test="${_option eq param.value}">checked</c:if>
    </c:set>
    <c:set var="_title" value="option.${_option}"/>
    <input type="radio" id="${param.name}${_option}" name="${param.name}" value="${_option}" ${_checked}/><label for="${param.name}${_option}">${param[_title]}</label>
  </c:forEach> 
</div>