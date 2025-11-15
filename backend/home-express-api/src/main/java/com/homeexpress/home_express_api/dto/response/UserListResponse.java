package com.homeexpress.home_express_api.dto.response;

import java.util.List;
import java.util.Map;

// Response cho danh sách users có phân trang
public class UserListResponse {
    
    private Object users; // Can be List<UserResponse> or List<Map<String, Object>>
    private long totalUsers;
    private int totalPages;
    private int currentPage;
    private Map<String, Object> pagination;

    public UserListResponse() {
    }

    public UserListResponse(List<UserResponse> users, long totalUsers, int totalPages, int currentPage) {
        this.users = users;
        this.totalUsers = totalUsers;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    // Getters and Setters
    public Object getUsers() {
        return users;
    }

    public void setUsers(Object users) {
        this.users = users;
    }

    public Map<String, Object> getPagination() {
        return pagination;
    }

    public void setPagination(Map<String, Object> pagination) {
        this.pagination = pagination;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
