package com.joejoe2.demo.data;

import com.joejoe2.demo.data.user.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class PageOfUserProfile {
    @Schema(description = "num of items in all pages")
    private long totalItems;
    @Schema(description = "current page")
    private int currentPage;
    @Schema(description = "num of total pages")
    private int totalPages;
    @Schema(description = "size of the page")
    private int pageSize;
    @Schema(description = "items in the page")
    private List<UserProfile> profiles;

    public PageOfUserProfile(PageList<UserProfile> pageList) {
        this.totalItems = pageList.getTotalItems();
        this.currentPage = pageList.getCurrentPage();
        this.totalPages = pageList.getTotalPages();
        this.pageSize = pageList.getPageSize();
        this.profiles = pageList.getList();
    }
}
