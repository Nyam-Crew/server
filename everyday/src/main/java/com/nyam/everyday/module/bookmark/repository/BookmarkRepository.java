package com.nyam.everyday.module.bookmark.repository;

import com.nyam.everyday.module.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark,Long> {

}
