<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%> 
<h1>Error</h1>
<em><c:out value="${exception.message}" /></em>
<p>Application has encountered an error. Please contact support on ...</p>

<!--
  Failed URL: ${url}
  Exception:  ${exception.message}
<c:forEach items="${exception.stackTrace}" var="ste">    ${ste} 
</c:forEach>
-->