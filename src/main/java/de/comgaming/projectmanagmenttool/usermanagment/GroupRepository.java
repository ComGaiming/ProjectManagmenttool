package de.comgaming.projectmanagmenttool.usermanagment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupname(String groupname);

    boolean existsByGroupname(String groupname);

    void deleteByGroupname(String groupname);
}
