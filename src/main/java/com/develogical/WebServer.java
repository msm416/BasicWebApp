package com.develogical;

import com.develogical.web.ApiResponse;
import com.develogical.web.IndexPage;
import com.develogical.web.ResultsPage;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {

  public WebServer() throws Exception {

    Server server = new Server(portNumberToUse());

    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(new ServletHolder(new Website()), "/*");
    handler.addServletWithMapping(new ServletHolder(new Api()), "/api/*");
    server.setHandler(handler);

    server.start();
  }


  static class Website extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String query = req.getParameter("q");
      if (query == null) {
        String queryOtherParam = req.getParameter("t");
        if(queryOtherParam == null) {
          new IndexPage().writeTo(resp);
        } else {
          int complexity = Integer.parseInt(queryOtherParam);
          new ResultsPage(queryOtherParam, new QueryProcessor(new ActualDBController()).suggestAMeal(complexity)).writeTo(resp);
        }
      } else {
        new ResultsPage(query, new QueryProcessor(new ActualDBController()).getNutritionalData(query)).writeTo(resp);
      }
    }
  }

  static class Api extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String query = req.getParameter("q");
      new ApiResponse(new QueryProcessor(new ActualDBController()).getNutritionalData(query)).writeTo(resp);
    }
  }

  private Integer portNumberToUse() {
    return System.getenv("PORT") != null ? Integer.valueOf(System.getenv("PORT")) : 8080;
  }

  public static void main(String[] args) throws Exception {
      new WebServer();
  }

}