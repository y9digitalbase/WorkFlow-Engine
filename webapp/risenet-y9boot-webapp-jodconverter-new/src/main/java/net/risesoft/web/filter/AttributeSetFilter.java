package net.risesoft.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import net.risesoft.config.ConfigConstants;
import net.risesoft.config.WatermarkConfigConstants;
import net.risesoft.utils.KkFileUtils;

public class AttributeSetFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        this.setWatermarkAttribute(request);
        this.setFileAttribute(request);
        filterChain.doFilter(request, response);
    }

    /**
     * 设置办公文具预览逻辑需要的属性
     *
     * @param request request
     */
    private void setFileAttribute(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        request.setAttribute("pdfPresentationModeDisable", ConfigConstants.getPdfPresentationModeDisable());
        request.setAttribute("pdfOpenFileDisable", ConfigConstants.getPdfOpenFileDisable());
        request.setAttribute("pdfPrintDisable", ConfigConstants.getPdfPrintDisable());
        request.setAttribute("pdfDownloadDisable", ConfigConstants.getPdfDownloadDisable());
        request.setAttribute("pdfBookmarkDisable", ConfigConstants.getPdfBookmarkDisable());
        request.setAttribute("fileKey", httpRequest.getParameter("fileKey"));
        request.setAttribute("switchDisabled", ConfigConstants.getOfficePreviewSwitchDisabled());
        request.setAttribute("fileUploadDisable", ConfigConstants.getFileUploadDisable());
        request.setAttribute("beian", ConfigConstants.getBeian());
        request.setAttribute("size", ConfigConstants.maxSize());
        request.setAttribute("deleteCaptcha", ConfigConstants.getDeleteCaptcha());
    }

    /**
     * 设置水印属性
     *
     * @param request request
     */

    private void setWatermarkAttribute(ServletRequest request) {
        String watermarkTxt = KkFileUtils.htmlEscape(request.getParameter("watermarkTxt"));
        request.setAttribute("watermarkTxt",
            watermarkTxt != null ? watermarkTxt : WatermarkConfigConstants.getWatermarkTxt());
        String watermarkXSpace = KkFileUtils.htmlEscape(request.getParameter("watermarkXSpace"));
        if (!KkFileUtils.isInteger(watermarkXSpace)) {
            watermarkXSpace = null;
        }
        request.setAttribute("watermarkXSpace",
            watermarkXSpace != null ? watermarkXSpace : WatermarkConfigConstants.getWatermarkXSpace());
        String watermarkYSpace = KkFileUtils.htmlEscape(request.getParameter("watermarkYSpace"));
        if (!KkFileUtils.isInteger(watermarkYSpace)) {
            watermarkYSpace = null;
        }
        request.setAttribute("watermarkYSpace",
            watermarkYSpace != null ? watermarkYSpace : WatermarkConfigConstants.getWatermarkYSpace());
        String watermarkFont = KkFileUtils.htmlEscape(request.getParameter("watermarkFont"));
        request.setAttribute("watermarkFont",
            watermarkFont != null ? watermarkFont : WatermarkConfigConstants.getWatermarkFont());
        String watermarkFontsize = KkFileUtils.htmlEscape(request.getParameter("watermarkFontsize"));
        request.setAttribute("watermarkFontsize",
            watermarkFontsize != null ? watermarkFontsize : WatermarkConfigConstants.getWatermarkFontsize());
        String watermarkColor = KkFileUtils.htmlEscape(request.getParameter("watermarkColor"));
        request.setAttribute("watermarkColor",
            watermarkColor != null ? watermarkColor : WatermarkConfigConstants.getWatermarkColor());
        String watermarkAlpha = KkFileUtils.htmlEscape(request.getParameter("watermarkAlpha"));
        if (!KkFileUtils.isInteger(watermarkAlpha)) {
            watermarkAlpha = null;
        }
        request.setAttribute("watermarkAlpha",
            watermarkAlpha != null ? watermarkAlpha : WatermarkConfigConstants.getWatermarkAlpha());
        String watermarkWidth = KkFileUtils.htmlEscape(request.getParameter("watermarkWidth"));
        if (!KkFileUtils.isInteger(watermarkWidth)) {
            watermarkWidth = null;
        }
        request.setAttribute("watermarkWidth",
            watermarkWidth != null ? watermarkWidth : WatermarkConfigConstants.getWatermarkWidth());
        String watermarkHeight = KkFileUtils.htmlEscape(request.getParameter("watermarkHeight"));
        if (!KkFileUtils.isInteger(watermarkHeight)) {
            watermarkHeight = null;
        }
        request.setAttribute("watermarkHeight",
            watermarkHeight != null ? watermarkHeight : WatermarkConfigConstants.getWatermarkHeight());
        String watermarkAngle = KkFileUtils.htmlEscape(request.getParameter("watermarkAngle"));
        if (!KkFileUtils.isInteger(watermarkAngle)) {
            watermarkAngle = null;
        }
        request.setAttribute("watermarkAngle",
            watermarkAngle != null ? watermarkAngle : WatermarkConfigConstants.getWatermarkAngle());
    }

    @Override
    public void destroy() {

    }
}
