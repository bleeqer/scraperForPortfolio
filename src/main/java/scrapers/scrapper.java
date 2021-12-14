package scrapers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;


public class scrapper {

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

                    System.out.println(row.html());
                    if (row.html().equals("NEXT")) {

                        if (row.attr("href").equals("#none")) {
                            continue;
                        }
                        int nextPageIndex = row.attr("href").lastIndexOf("=") + 1;

                        nextPage = Integer.parseInt(row.attr("href").substring(nextPageIndex));

                        System.out.println("nextPage is " + nextPage);
                    }
                }

                final Document detailDocument = Jsoup.connect(searchUrl + "page=" + currentPage).get();

                for (Element row : detailDocument.select(
                        "ul.prdList.grid4 div.thumbnail a")) {
                    if (row.attr("href") != "") {
                        System.out.println(row.attr("href"));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
