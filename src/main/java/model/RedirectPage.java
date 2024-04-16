package model;

public class RedirectPage extends Page{


    public RedirectPage(String contents) {
        super(contents);
        this.pageType = "redirect";
    }

}
