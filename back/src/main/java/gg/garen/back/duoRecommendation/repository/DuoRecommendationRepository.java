package gg.garen.back.duoRecommendation.repository;

import gg.garen.back.duoRecommendation.entity.DuoRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DuoRecommendationRepository extends JpaRepository<DuoRecord, Integer> {

    //따로 엔티티를 안만들고 네이티브 쿼리로 실행한다.
    @Query(value = "SELECT name, champion_key, id FROM champion", nativeQuery = true)
    List<Object[]> findAllNamesAndKeysAndIds();
    @Query(value = "SELECT * " +
            "FROM duo_record d " +
            "where ((d.champion1 = :championKey AND d.lane1 = :position )  OR (d.champion2 = :championKey AND d.lane2 = :position)) AND d.total_match >= 10 AND d.win_rate >= 50 " +
            "order by victory desc, d.win_rate desc", nativeQuery = true)
    List<DuoRecord> findDuoRecordByChampionName(@Param("championKey") Long championKey, @Param("position") String position);
}
