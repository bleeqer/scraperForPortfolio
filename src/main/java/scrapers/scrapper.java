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




public class scrapper {

    private static String baseUrl = "https://modernif.co.kr";
    private static final String searchUrl = "https://modernif.co.kr/product/list.html?cate_no=24&";

    private static String IMAGE_DESTINATION_FOLDER = "C:/images";

    public static void main(String [] args) {

        int currentPage = 0;
        int nextPage = 1;

        try {

            while (currentPage <= nextPage) {

                currentPage += 1;

                final Document document = Jsoup.connect(searchUrl + "page=" + currentPage).get();
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
                        String productPrice = detailDocument.select("#detail_wrap div div.detailArea div div.product_info ul li.price").html();
                        String productImage = detailDocument.select("#detail_wrap div div.xans-element-.xans-product.xans-product-image.imgArea div img").attr("src").substring(detailDocument.select("#detail_wrap div div.xans-element-.xans-product.xans-product-image.imgArea div img").attr("src").lastIndexOf("/") + 1);

                        downloadImage(detailDocument.select("#detail_wrap div div.xans-element-.xans-product.xans-product-image.imgArea div img").attr("src"));

                        System.out.println(productName + " " + productPrice + " " + productImage);


                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // image download
    private static void downloadImage(String strImageURL) {

        // get the file name
        String strImageName = strImageURL.substring(strImageURL.lastIndexOf("/") + 1);

        System.out.println("Saving: " + strImageName + ", from: + strImageURL");

        try {

            URL urlImage = new URL(strImageURL);
            InputStream in = urlImage.openStream();

            byte[] buffer = new byte[4096];
            int n = -1;

            OutputStream os = new FileOutputStream(IMAGE_DESTINATION_FOLDER + "/" + strImageName);

            while ((n = in.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }

            os.close();

            System.out.println("IMAGE SAVED");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
