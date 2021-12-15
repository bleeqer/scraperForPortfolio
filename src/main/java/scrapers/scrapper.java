package scrapers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class scrapper {

    private static String baseUrl = "https://modernif.co.kr";
    private static final String searchUrl = "https://modernif.co.kr/product/list.html?cate_no=24&";

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

                        System.out.println(productName + " " + productPrice + " " + productImage);


                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
