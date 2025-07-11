package com.rhizomind.quickapp.repo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepoList {

    private List<Repo> repositories = new java.util.ArrayList<>();

}
