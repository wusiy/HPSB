package com.sunland.cpocr.xlw;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// USB数据解析器
public class USBDataParser {

    // 数据类型
    public class CAMERA_INFO_TYPE
    {
        public static final int CAMERA_THROB = 0xFFFF0001;            // 心跳包
        public static final int CAMERA_THROB_RESPONSE = 0xFFFF0002;   // 心跳回包
        public static final int CAMERA_IMAGE = 0xF0000001;
        public static final int CAMERA_VIDEO = 0xF0000002;            // H.264视频帧包
        public static final int CAMERA_AUDIO = 0xF0000003;
        public static final int CAMERA_RECORD = 0xF0010001;           // 结果包
        public static final int CAMERA_STRING = 0xF0010002;           // 附加信息包
        public static final int CAMERA_STATE = 0xF0020001;
    }

    // 结果图片类型
    public class RECORD_IMAGE_TYPE
    {
        public static final int RECORD_IMAGE_BEST_SNAPSHOT = 0;	    // 最清晰大图      注：数据格式为Jpeg
        public static final int RECORD_IMAGE_LAST_SNAPSHOT = 1;	    // 最后大图       注：数据格式为Jpeg
        public static final int RECORD_IMAGE_BEGIN_CAPTURE = 2;	    // 第一张抓拍图    注：数据格式为Jpeg
        public static final int RECORD_IMAGE_BEST_CAPTURE = 3;	    // 第二张抓拍图    注：数据格式为Jpeg
        public static final int RECORD_IMAGE_LAST_CAPTURE = 4;	    // 第三张抓拍图    注：数据格式为Jpeg
        public static final int RECORD_IMAGE_SMALL_IMAGE = 5;	    // 车牌小图       注：数据格式为YUV422
        public static final int RECORD_IMAGE_BIN_IMAGE = 6;		    // 车牌二值图     注：数据格式为二进制
        public static final int PACKET_RECORD_VIDEO_ILLEGAL = 0x80000001;	// 违法视频数据流
        public static final int PACKET_RECORD_AUDIO_ILLEGAL = 0x80000002;	// 违法音频数据流
    }

    // 图片数据结构
    public class SWImage
    {
        public long lTime;   // 时间戳
        public int iWidth;   // 宽度
        public int iHeight;  // 高度
        public int iPlatePosTop;     // 车牌在图中坐标
        public int iPlatePosLeft;
        public int iPlatePosBottom;
        public int iPlatePosRight;
        public byte[] imageData;   // 图片数据
    }

    // 结果数据结构体
    public class OBCResult
    {
        public String m_strID;  // 结果ID
        public String m_strPlateNum = "";  // 车牌号
        public long m_lTime;  // 结果时间戳
        public String m_strAppendInfo = "";  // 结果具体信息
        public SWImage m_SmallImage = new SWImage();  // 小图数据
        public List<SWImage> m_BigImageList = new ArrayList<SWImage>();  // 大图数据队列
    }
    public OBCResult m_result = new OBCResult(); // 结果数据

    // 视频帧数据
    public class OBCVideo
    {
        public boolean m_bKey;           // 是否是I帧
        public int m_iWidth;             // 宽度
        public int m_iHeight;            // 高度
        public long m_lTime;             // 时间戳
        public int m_iShutter;           // 快门
        public int m_iGain;              // 增益
        public int m_iRGain;             // 红增益
        public int m_iGGain;             // 绿增益
        public int m_iBGain;             // 蓝增益
        public String m_strType;         // 帧类型：H264
        public String m_strAppendInfo = "";  // 具体信息字符串
        public byte[] m_VideoData;  // 实际帧数据
    }

    // 对外接口::解析结果数据
    public int putInResultData(@NotNull byte[] bytes) {
        if (bytes.length < 12) {
            return -1;
        }

        if (m_result != null) {
            m_result = null;
            m_result = new OBCResult();
        }

        // 解析包头12字节
        byte[] data = new byte[12];
        System.arraycopy(bytes, 0, data, 0, 12);

        // 拆解头信息
        byte[] temp = new byte[4];
        System.arraycopy(data, 0, temp, 0, 4);
        int iType = byteArrayToInt(temp); // 类型

        System.arraycopy(data, 4, temp, 0, 4);
        int iXmlLen = byteArrayToInt(temp); // xml长度

        System.arraycopy(data, 8, temp, 0, 4);
        int iDataLen = byteArrayToInt(temp); // 内容数据长度

        // 如果是附加信息类型
        if (iType == CAMERA_INFO_TYPE.CAMERA_STRING) {
            return 1;
        }

        // 非结果类型
        if (iType != CAMERA_INFO_TYPE.CAMERA_RECORD) {
            return -1;
        }

        // 解析xml
        byte[] xmlData = new byte[iXmlLen];
        System.arraycopy(bytes, 12, xmlData, 0, iXmlLen);
        m_result.m_strAppendInfo = xmlData.toString();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xmlData);
            Document doc = builder.parse(is);
            Element rootElement = doc.getDocumentElement();

            Element resultSetElement = (Element)rootElement.getElementsByTagName("ResultSet").item(0);
            Element resultElement = (Element)resultSetElement.getElementsByTagName("Result").item(0);

            Element element = (Element)resultSetElement.getElementsByTagName("TimeHigh").item(0);
            String strTimeHight = element.getAttribute("value");
            element = (Element)resultSetElement.getElementsByTagName("TimeLow").item(0);
            String strTimeLow = element.getAttribute("value");
            m_result.m_lTime = Integer.parseInt(strTimeHight);
            m_result.m_lTime <<= 32;
            m_result.m_lTime += Integer.parseInt(strTimeLow) & 0xFFFFFFFFL;

            element = (Element)resultSetElement.getElementsByTagName("CarID").item(0);
            m_result.m_strID = element.getAttribute("value");
            element = (Element)resultSetElement.getElementsByTagName("PlateName").item(0);
            m_result.m_strPlateNum = element.getTextContent();

            // 解析图片数据
            Element extInfoElement = (Element)rootElement.getElementsByTagName("ResultExtInfo").item(0);
            byte[] imageData = new byte[iDataLen];
            System.arraycopy(bytes, 12 + iXmlLen, imageData, 0, iDataLen);
            int iPos = 0;

            // 循环拆解图片数据
            while (iPos < imageData.length) {
                byte[] imageInfoTemp = new byte[4];
                System.arraycopy(imageData, iPos, imageInfoTemp, 0, 4);
                int iImageType = byteArrayToInt(imageInfoTemp); // 类型(位置)
                System.arraycopy(imageData, iPos + 4, imageInfoTemp, 0, 4);
                int iImageLen = byteArrayToInt(imageInfoTemp); // 长度
                iPos += 8;

                if (iImageLen > imageData.length) {
                    Log.e("USB", String.format("[USB]解析图片异常: %d ", iImageLen));
                    return -1;
                }

                switch (iImageType) {
                    // 大图
                    case RECORD_IMAGE_TYPE.RECORD_IMAGE_BEST_SNAPSHOT:
                    case RECORD_IMAGE_TYPE.RECORD_IMAGE_LAST_SNAPSHOT:
                    case RECORD_IMAGE_TYPE.RECORD_IMAGE_BEGIN_CAPTURE:
                    case RECORD_IMAGE_TYPE.RECORD_IMAGE_BEST_CAPTURE:
                    case RECORD_IMAGE_TYPE.RECORD_IMAGE_LAST_CAPTURE: {
                        String strImagePos = String.format("Image%d", iImageType);
                        Element bigElement = (Element)extInfoElement.getElementsByTagName(strImagePos).item(0);

                        SWImage bigImage = new SWImage();
                        bigImage.iWidth = Integer.parseInt(bigElement.getAttribute("Width"));
                        bigImage.iHeight = Integer.parseInt(bigElement.getAttribute("Height"));
                        bigImage.iPlatePosTop = Integer.parseInt(bigElement.getAttribute("PlatePosTop"));
                        bigImage.iPlatePosLeft = Integer.parseInt(bigElement.getAttribute("PlatePosLeft"));
                        bigImage.iPlatePosBottom = Integer.parseInt(bigElement.getAttribute("PlatePosBottom"));
                        bigImage.iPlatePosRight = Integer.parseInt(bigElement.getAttribute("PlatePosRight"));
                        bigImage.lTime = Integer.parseInt(bigElement.getAttribute("TimeHigh"));
                        bigImage.lTime <<= 32;
                        bigImage.lTime += Integer.parseInt(bigElement.getAttribute("TimeLow")) & 0xFFFFFFFFL;

                        bigImage.imageData = new byte[iImageLen];
                        System.arraycopy(imageData, iPos, bigImage.imageData, 0, iImageLen);
                        Log.i("USB", "解析图片：" + iImageLen);
                        m_result.m_BigImageList.add(bigImage);
                    }
                    break;
                    // 小图
                    case RECORD_IMAGE_TYPE.RECORD_IMAGE_SMALL_IMAGE: {
                        Element smallElement = (Element)extInfoElement.getElementsByTagName("Image5").item(0);
                        m_result.m_SmallImage.iWidth = Integer.parseInt(smallElement.getAttribute("Width"));
                        m_result.m_SmallImage.iHeight = Integer.parseInt(smallElement.getAttribute("Height"));
                        m_result.m_SmallImage.lTime = Integer.parseInt(smallElement.getAttribute("TimeHigh"));
                        m_result.m_SmallImage.lTime <<= 32;
                        m_result.m_SmallImage.lTime += Integer.parseInt(smallElement.getAttribute("TimeLow")) & 0xFFFFFFFFL;
                        m_result.m_SmallImage.imageData = new byte[iImageLen];
                        System.arraycopy(imageData, iPos, m_result.m_SmallImage.imageData, 0, iImageLen);
                    }
                    break;
                    // 二值图
                    case RECORD_IMAGE_TYPE.RECORD_IMAGE_BIN_IMAGE: {
                        Element binElement = (Element)extInfoElement.getElementsByTagName("Image6").item(0);
                    }
                    break;
                    // 违法视频
                    case RECORD_IMAGE_TYPE.PACKET_RECORD_VIDEO_ILLEGAL: {
                        Element videoElement = (Element)extInfoElement.getElementsByTagName("Video").item(0);
                    }
                    break;
                    // 违法音频
                    case RECORD_IMAGE_TYPE.PACKET_RECORD_AUDIO_ILLEGAL: {
                        Element audioElement = (Element)extInfoElement.getElementsByTagName("Audio").item(0);
                    }
                    break;
                }

                // 偏移至下一数据段
                iPos += iImageLen;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    // 对外接口::解析视频帧数据
    public OBCVideo putInVideoData(@NotNull byte[] bytes) {
        if (bytes.length < 12) {
            return null;
        }

        // 解析包头12字节
        byte[] data = new byte[12];
        System.arraycopy(bytes, 0, data, 0, 12);

        // 拆解头信息
        byte[] temp = new byte[4];
        System.arraycopy(data, 0, temp, 0, 4);
        int iType = byteArrayToInt(temp); // 类型

        System.arraycopy(data, 4, temp, 0, 4);
        int iXmlLen = byteArrayToInt(temp); // xml长度

        System.arraycopy(data, 8, temp, 0, 4);
        int iDataLen = byteArrayToInt(temp); // 内容数据长度

        // 非视频类型
        if (iType != CAMERA_INFO_TYPE.CAMERA_VIDEO) {
            return null;
        }

        // 视频帧
        OBCVideo obcVideo = new OBCVideo();

        // 解析xml
        byte[] xmlData = new byte[iXmlLen];
        System.arraycopy(bytes, 12, xmlData, 0, iXmlLen);
        obcVideo.m_strAppendInfo = xmlData.toString();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xmlData);
            Document doc = builder.parse(is);
            Element rootElement = doc.getDocumentElement();

            Element videoElement = (Element)rootElement.getElementsByTagName("Video").item(0);
            obcVideo.m_strType = videoElement.getAttribute("Type");
            if (!obcVideo.m_strType.contains("H264")) {
                return null;
            }

            obcVideo.m_lTime = Integer.parseInt(videoElement.getAttribute("TimeHigh"));
            obcVideo.m_lTime <<= 32;
            obcVideo.m_lTime += Integer.parseInt(videoElement.getAttribute("TimeLow")) & 0xFFFFFFFFL;

            obcVideo.m_iWidth = Integer.parseInt(videoElement.getAttribute("Width"));
            obcVideo.m_iHeight = Integer.parseInt(videoElement.getAttribute("Height"));
            obcVideo.m_iShutter= Integer.parseInt(videoElement.getAttribute("Shutter"));
            obcVideo.m_iGain = Integer.parseInt(videoElement.getAttribute("Gain"));
            obcVideo.m_iRGain = Integer.parseInt(videoElement.getAttribute("r_Gain"));
            obcVideo.m_iGGain = Integer.parseInt(videoElement.getAttribute("g_Gain"));
            obcVideo.m_iBGain = Integer.parseInt(videoElement.getAttribute("b_Gain"));

            String strFrameType = videoElement.getAttribute("FrameType");
            obcVideo.m_bKey = strFrameType.contains("IFrame");

            obcVideo.m_VideoData = new byte[iDataLen];
            System.arraycopy(bytes, 12 + iXmlLen, obcVideo.m_VideoData, 0, iDataLen);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return obcVideo;
    }

    // byte[]转int
    public static int byteArrayToInt(byte[] bytes) {
        int value;
        value = (int) ((bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF) << 8)
                | ((bytes[2] & 0xFF) << 16)
                | ((bytes[3] & 0xFF) << 24));
        return value;
    }
}
