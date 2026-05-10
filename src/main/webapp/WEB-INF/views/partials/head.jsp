<%-- 
    head.jsp: Shared meta tags and font loading.
    USAGE: Call request.setAttribute("_pageTitle","...") and request.setAttribute("_pageCSS","css-name")
    before including this file. Do NOT put a page contentType directive here.
--%>
<%
    String _pageTitle = (String) request.getAttribute("_pageTitle");
    if (_pageTitle == null || _pageTitle.trim().isEmpty()) _pageTitle = "FashionStore";
    String _pageCSS   = (String) request.getAttribute("_pageCSS");
%>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="FashionStore - premium fashion marketplace with curated styles for every season.">
<title><%= _pageTitle %> | FashionStore</title>

<%-- Favicon & Brand Icons --%>
<link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/assets/images/logo-mark.svg">
<link rel="apple-touch-icon" href="<%= request.getContextPath() %>/assets/images/logo-mark.svg">

<%-- Google Fonts: ONE place, no @import --%>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500&family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

<%-- Design system ALWAYS loads first (correct order) --%>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/design-tokens.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/reset.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/base.css">

<%-- Global context and CSRF must be set before any external script reads them --%>
<script>
    window.contextPath = '<%= request.getContextPath() %>';
    window.csrfToken = '<%= request.getAttribute("csrfToken") != null ? org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(request.getAttribute("csrfToken").toString()) : "" %>';
</script>

<%-- Main JavaScript --%>
<script src="<%= request.getContextPath() %>/assets/js/splash-screen.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/animations.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/cart.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/lazy-loading.js"></script>

<%-- Core component styles (single source of truth) --%>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/components/navbar.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/components/product-card.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/components/footer.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/components/buttons.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/components/forms.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/components/mobile-nav.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/search-suggestions.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/filter-chips.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/toast-premium.css">

<%-- Page-level CSS (comma-separated filenames) --%>
<% if (_pageCSS != null && !_pageCSS.trim().isEmpty()) {
       for (String _css : _pageCSS.split(",")) {
           _css = _css.trim();
           if (!_css.isEmpty()) { %>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/<%= _css %>.css">
<%     }
   }
} %>
<%-- Unified CSS architecture ends above; no overlay layers. --%>
