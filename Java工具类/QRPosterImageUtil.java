package com.cheniue.app.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.UUID;

import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;

/**
 * @desc 制作包含二维码的图片海报工具类，将一张底图加上二维码合成成一个海报用于推广
 * @author zhuchong
 */
public class QRPosterImageUtil {

    public static String CHARSET = "UTF-8";
    //二维码的宽度
    public static final int QRCodeWidth = 486;
    //二维码的长度
    public static final int QRCodeHeight = 486;

    //字体颜色
    public static final Color FONT_COLOR = new Color(21,21,21);
    //字体信息
    public static final Font FONT_INFO = new Font("宋体",Font.PLAIN,84);

    /**
     * 生成二维码
     * @param content 二维码内容
     * @param logoImgPath 中间logo地址,不需要就填null
     * @param needCompress 是否压缩 如果没有logo，这个参数不起作用
     * @return
     * @throws Exception
     */
    public static BufferedImage createQRCodeImage( String content, String logoImgPath, boolean needCompress) throws Exception{
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        //200是定义的二维码或小图片的大小
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeHeight, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //循环遍历每一个像素点
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        // 没有logo
        if (logoImgPath == null || "".equals(logoImgPath)) {
            return image;
        }

        // 插入logo
        insertImage(image, logoImgPath, needCompress);
        return image;
    }

    /**
     * 二维码插入logo
     * @param source 生成的二维码BufferedImage对象
     * @param logoImgPath  logo地址
     * @param needCompress 是否压缩，true-是，false-否
     * @throws IOException
     */
    public static void insertImage(BufferedImage source, String logoImgPath, boolean needCompress) throws IOException {
        File file = new File(logoImgPath);
        if (!file.exists()) {
            return;
        }

        Image src = ImageIO.read(new File(logoImgPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        //处理logo
        if (needCompress) {
            if (width > WIDTH) {
                width = WIDTH;
            }

            if (height > HEIGHT) {
                height = HEIGHT;
            }

            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics gMaker = tag.getGraphics();
            gMaker.drawImage(image, 0, 0, null); // 绘制缩小后的图
            gMaker.dispose();
            src = image;
        }

        // 在中心位置插入logo
        Graphics2D graph = source.createGraphics();
        int x = (200 - width) / 2;
        int y = (200 - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }


    /**
     * 合成二维码为图片
     * @param backPicPath 背景图片地址
     * @param code 生成的二维码对象
     */
    public static final void combineCodeAndPicToFile(String backPicPath, BufferedImage code/*String fillPicPath*/) {
        try {
            BufferedImage big = ImageIO.read(new File(backPicPath));
            BufferedImage small = code;
        /*//合成两个文件时使用
        BufferedImage small = ImageIO.read(new File(fillPicPath));*/
            Graphics2D g = big.createGraphics();
            int fontLeft = 643;//758
            int fontHeight = 429;//390
            //设置左上角文字
            g.setColor(FONT_COLOR);
            g.setFont(FONT_INFO);
            g.drawString("瑞丽斯",fontLeft,fontHeight); //画文字


            //二维码或小图在大图的左上角坐标
            //int x = (big.getWidth() - small.getWidth()) / 2;//中心坐标
            //int y = (big.getHeight() - small.getHeight() - 100);

            int x=1559;
            int y = 182;
            g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
            g.dispose();
            //为了保证大图背景不变色，formatName必须为"png"
            String newPictureName = UUID.randomUUID().toString();
            ImageIO.write(big, "png", new File("C:/Users/10431/Desktop/qecode/"+newPictureName+".png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 合成二维码和图片为输出流，可用于下载或直接展示
     * @param backPicPath 背景图片地址
     * @param code
     * @param resp
     * @throws Exception
     */
    public static final void combineCodeAndPicToInputstream(String backPicPath, BufferedImage code, HttpServletResponse resp, String user_name) throws Exception {
        File file  = new File(backPicPath);
        file.exists();
        BufferedImage big = ImageIO.read(file);
        // BufferedImage small = ImageIO.read(new File(fillPicPath));
        BufferedImage small = code;
        Graphics2D g = big.createGraphics();

        int fontLeft = 643;//758
        int fontHeight = 429;//390
        //设置左上角文字
        g.setColor(FONT_COLOR);
        g.setFont(FONT_INFO);
        g.drawString(user_name,fontLeft,fontHeight); //画文字

        //二维码或小图在大图的左上角坐标
        int x=1559;
        int y = 182;   //二维码距大图下边距100
        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
        g.dispose();
        String newPictureName = UUID.randomUUID().toString()+".png";
        resp.addHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode(newPictureName,"UTF-8") );//去掉这行设置header的代码，前端访问可以直接展示
        //为了保证大图背景不变色，formatName必须为"png"
        ServletOutputStream outputStream = resp.getOutputStream();
        ImageIO.write(big, "png", outputStream);
    }



    public static void main(String[] args) throws Exception {
        BufferedImage qrCodeImage = createQRCodeImage( "https://www.baidu.com/", null, false);
        combineCodeAndPicToFile("C:\\Users\\10431\\Desktop\\qecode\\test.png", qrCodeImage);
    }

}
