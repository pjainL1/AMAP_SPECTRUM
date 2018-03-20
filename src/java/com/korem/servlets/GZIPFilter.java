/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.korem.servlets;

import com.lo.config.Confs;
import java.io.*;
import javax.servlet.http.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

/**
 *
 * @author agilbert
 */
public class GZIPFilter implements Filter {

    private static final Logger log = ESAPI.getLogger(GZIPFilter.class);
  
  private boolean doGzip(HttpServletRequest request) {
      String requestURI = request.getRequestURI();
      
      for (String path : Confs.STATIC_CONFIG.outputGzipExcludedPathsSet()) {
          if (requestURI.indexOf(path) != -1) {
              return false;
          }
      }
      
      return true;
  }
    
    @Override
  public void doFilter(ServletRequest req, ServletResponse res,
      FilterChain chain) throws IOException, ServletException {
    if (req instanceof HttpServletRequest) {
        //System.out.println("inside dofilter");
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;
      
      if(doGzip(request)){
          String ae = request.getHeader("accept-encoding");
          if (ae != null && ae.indexOf("gzip") != -1) {
            GZIPResponseWrapper wrappedResponse =
              new GZIPResponseWrapper(response);
            chain.doFilter(req, wrappedResponse);
            wrappedResponse.finishResponse();
            return;
          }
      }else{
//          String uri = ESAPI.encoder().encodeForOS(new WindowsCodec(), request.getRequestURI());
          log.debug(ESAPI.log().SECURITY,false,"no gzip for you: " + request.getRequestURI());
      }
      chain.doFilter(req, res);
    }
  }

  public void init(FilterConfig filterConfig) {
    // noop
  }

  public void destroy() {
    // noop
  }
}