package scrapers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class scrapper {

    private static String baseUrl = "https://modernif.co.kr";
    private static final String searchUrl = "https://modernif.co.kr/category/";
    public static String[][] cateList = {
            {"자켓", "코트", "점퍼짚업"},
            {"43/", "47/", "46/"}
    };
    public static HashMap<String, String> detailMap = new HashMap(10);

    private static String IMAGE_DESTINATION_FOLDER = "C:/images";

    public static void main(String [] args) {

        int currentPage = 0;
        int nextPage = 1;

        try {

            while (currentPage <= nextPage) {

                currentPage += 1;

                final Document document = Jsoup.connect(searchUrl + cateList[0][0] + "/" + cateList[1][0] + "?page=" + currentPage).get();
                for (Element row : document.select(
                                "div.xans-element-.xans-product.xans-product-normalpaging.ec-base-paginate-text a"
                        )) {

                    if (row.html().equals("NEXT")) {

                        if (row.attr("href").equals("#none")) {
                            continue;
                        }
                        int nextPageIndex = row.attr("href").lastIndexOf("=") + 1;

                        nextPage = Integer.parseInt(row.attr("href").substring(nextPageIndex));

                    }
                }

                /*final Document nextDocument = Jsoup.connect(searchUrl + "page=" + currentPage).get();*/

                for (Element row : document.select(
                        "ul.prdList.grid4 div.thumbnail a")) {
                    if (!row.attr("href").equals("")) {

                        String restUrl = row.attr("href");


                        final Document detailDocument = Jsoup.connect(baseUrl + restUrl).get();

                        String productName = detailDocument.select("#detail_wrap div div.detailArea div div.product_info ul li.name").html();
                        
                        if (productName.contains("수트") || productName.contains("세트")) {
                            continue;
                        }

                        String productPrice = detailDocument.select("#detail_wrap div div.detailArea div div.product_info ul li.price").html();

                        String productThumbmail = detailDocument.select("#detail_wrap div div.xans-element-.xans-product.xans-product-image.imgArea div img").attr("src").substring(detailDocument.select("#detail_wrap div div.xans-element-.xans-product.xans-product-image.imgArea div img").attr("src").lastIndexOf("/") + 1);


                        // 상품 상세 사진 2장
                        Element productDetailPhoto1 = (detailDocument.select("#prdDetail div.cont img").first());
                        Element productDetailPhoto2 = (detailDocument.select("#prdDetail div.cont img").last());

                        // 상품 상세 사진 2장 소스
                        String imageRollURI1 = baseUrl + (productDetailPhoto1.attr("src").substring(productDetailPhoto1.attr("src").lastIndexOf("/web")));
                        String imageRollURI2 = baseUrl + (productDetailPhoto2.attr("src").substring(productDetailPhoto2.attr("src").lastIndexOf("/web")));

                        // 상품 썸네일
                        String thumbnail = baseUrl + detailDocument.select("#detail_wrap div div.xans-element-.xans-product.xans-product-image.imgArea div img").attr("src").substring(detailDocument.select("#detail_wrap div div.xans-element-.xans-product.xans-product-image.imgArea div img").attr("src").lastIndexOf("/web"));

                        // 상품 상세 사진 2장 다운로드 메소드에 전달할 Array
                        String[] productDetailPhotosNameArr = {imageRollURI1, imageRollURI2};
                        // 상품 썸네일 다운로드 메소드에 전달할 Array
                        String[] thumbnailPhotoArr = {thumbnail};

                        // 상품 설명
                        String productDescription = detailDocument.select("#tab2 div ul li pre").html();

                        // 상품 설명 비어있는 상품 제외하기
                        if (productDescription.equals("")) {
                            continue;
                        }
                        // 불필요한 태그 포함된 텍스트
                        String productCommentText = detailDocument.select("#tab3 div").html();


                        // 불필요한 텍스트에서 <p>, </p> 지우기
                        productCommentText = productCommentText.replace("<p>", "")
                                                                           .replace("</p>", "")
                                                                           .replace("<span style=\"font-size: 9pt;\">", "")
                                                                           .replace("</span>", "")
                                                                           .replace("&nbsp;", "")
                                                                           .replace("<br>", "\n");

                        // div 태그 포함된 상품, 코멘트 없는 상품 제외하기
                        if (productCommentText.contains("<div>") || productCommentText.contains("휴무")) {
                            continue;
                        }
//                        String productComment = detailDocument.select("#tab3 div p").html().replace("<br>", "");





//                        for (Element element : productDetailPhotos) {
//                            productDetailPhotosNameArr[0]
//                            System.out.println(element.attr("src"));
//                        }
                        System.out.println("-------------------------------------------------");

                        // 상품 사이즈
                        Elements productSize = (detailDocument.select("#tab1 div ul li table tbody tr"));


                        // 알 수 없는 이유로 tr 로드 안될 때 continue
                        if (productSize.size() <= 0) {
                            continue;
                        }

                        // 칼럼 부분 삭제
                        productSize.remove(0);

                        // 사이즈 Elements length 구하기
                        int maxSize = productSize.size();

//                      주의사항 지우기
                        productSize.remove(maxSize - 1);

                        // 프라덕트 테이블
                        detailMap.put("productName", productName);
                        detailMap.put("productPrice", productPrice.replace(",", "").replace("원", ""));
                        detailMap.put("imageRoll", downloadImage(productDetailPhotosNameArr));
                        detailMap.put("thumbnail", downloadImage(thumbnailPhotoArr));
                        detailMap.put("cateCode", "OU-JA");
                        detailMap.put("description", productDescription);
                        detailMap.put("comment", productCommentText);
                        detailMap.put("kind", "product");
                        connectDB(detailMap);

                        System.out.println(detailMap);
                        System.out.println("----------------------------------------------------------");

                        // 한 사이즈당 치수이므로 반복마다 데이터베이스에 제품명을 외래키로 하여 인서트할 것
                        for (Element tr : productSize) {

                            Elements values = tr.select("td span");

                            if (values.size() < 5) {
                                continue;
                            }


//                            -------------------------------------------------------------------------------
                            // 사이즈 테이블
                            detailMap.put("size", values.get(0).html());
                            detailMap.put("shoulder", values.get(1).html());
                            detailMap.put("chest", values.get(2).html());
                            detailMap.put("sleeves", values.get(3).html());
                            detailMap.put("length", values.get(4).html());
                            detailMap.put("product", productName);
                            detailMap.put("kind", "size");

                            // 사이즈데이터 데이터베이스에 추가
                            if (!detailMap.containsValue("<br>") && !detailMap.containsValue("허벅지")) {
                                System.out.println(detailMap);
                                connectDB(detailMap);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 썸네일과 디테일 이미지 2장 저장 메소드
    private static String downloadImage(String[] ImageURLsArr) {

        String uniqueID = UUID.randomUUID().toString();
        // get the file name
//        String strImageName = strImageURL.substring(strImageURL.lastIndexOf("/") + 1);
        byte[] buffer = new byte[4096];
        if (ImageURLsArr.length == 1) {
            try {
                URL urlImage = new URL(ImageURLsArr[0]);
                InputStream in = urlImage.openStream();

                int n = -1;
                OutputStream os = new FileOutputStream(IMAGE_DESTINATION_FOLDER + "/" + uniqueID + ".jpg");

                while ((n = in.read(buffer)) != -1) {
                    os.write(buffer, 0, n);
                }

                os.close();

                System.out.println("IMAGE SAVED");


            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (ImageURLsArr.length == 2) {

            try {
                URL urlImage1 = new URL(ImageURLsArr[0]);
                URL urlImage2 = new URL(ImageURLsArr[1]);
                InputStream in1 = urlImage1.openStream();
                InputStream in2 = urlImage2.openStream();

                int n = -1;


                OutputStream os1 = new FileOutputStream(IMAGE_DESTINATION_FOLDER + "/" + uniqueID + ".jpg");
                OutputStream os2 = new FileOutputStream(IMAGE_DESTINATION_FOLDER + "/" + uniqueID + "_2" + ".jpg");

                while ((n = in1.read(buffer)) != -1) {
                    os1.write(buffer, 0, n);
                }

                os1.close();

                while ((n = in2.read(buffer)) != -1) {
                    os2.write(buffer, 0, n);
                }

                os2.close();

                System.out.println("IMAGE SAVED");



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uniqueID + ".jpg";
    }

    private static  void connectDB(HashMap<String, String> detailMap) {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";

        String user = "portfolio";
        String password = "portfolio";

        String sizeSql = "insert into tbl_outersize values (?, ?, ?, ?, ?, ?)";
        String productSql = "insert into tbl_product values (?, ?, ?, ?, ?, ?, ?)";


        if (detailMap.get("kind").equals("size")) {
            try {
                DriverManager.registerDriver(new oracle.jdbc.OracleDriver());

                Connection con = DriverManager.getConnection(url, user, password);

                PreparedStatement sizePstmt = con.prepareStatement(sizeSql);

                sizePstmt.setString(1, detailMap.get("size").toString());
                sizePstmt.setString(2, detailMap.get("shoulder").toString());
                sizePstmt.setString(3, detailMap.get("chest").toString());
                sizePstmt.setString(4, detailMap.get("sleeves").toString());
                sizePstmt.setString(5, detailMap.get("length").toString());
                sizePstmt.setString(6, detailMap.get("product").toString());

    //          sql문 수행 및 결괏값 받기
                int message = sizePstmt.executeUpdate();

                if (message == 1) {
                    System.out.println("inserted successfully: " + detailMap.get("product").toString());
                } else {
                    System.out.println("insertion failed!!!" + detailMap.get("product").toString());
                }

                con.close();
            } catch (Exception ex) {
                System.err.println(ex);
            }

        } else if (detailMap.get("kind").equals("product")) {
            try {
                DriverManager.registerDriver(new oracle.jdbc.OracleDriver());

                Connection con = DriverManager.getConnection(url, user, password);

                PreparedStatement productPstmt = con.prepareStatement(productSql);



                productPstmt.setString(1, detailMap.get("productName").toString());
                productPstmt.setString(2, detailMap.get("description").toString());
                productPstmt.setString(3, detailMap.get("imageRoll").toString());
                productPstmt.setString(4, detailMap.get("thumbnail").toString());
                productPstmt.setString(5, detailMap.get("cateCode").toString());
                productPstmt.setString(6, detailMap.get("productPrice").toString());
                productPstmt.setString(7, detailMap.get("comment").toString());

                //          sql문 수행 및 결괏값 받기
                int message = productPstmt.executeUpdate();

                if (message == 1) {
                    System.out.println("inserted successfully: " + detailMap.get("productName").toString());
                } else {
                    System.out.println("insertion failed!!!" + detailMap.get("productName").toString());
                }
                con.close();
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }

    }
}
