package com.nyam.everyday.module.user.repository;

import com.nyam.everyday.module.user.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembersRepository extends JpaRepository<Members,Long> {

}
