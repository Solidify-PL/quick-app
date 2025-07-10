package com.rhizomind.quickapp.repo;

import lombok.Data;

import java.util.List;

@Data
public class RepoList {

    private List<Repo> repositories = new java.util.ArrayList<>();

}
