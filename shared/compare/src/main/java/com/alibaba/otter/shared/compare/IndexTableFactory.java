//package com.alibaba.otter.shared.compare;
//
//import com.alibaba.otter.shared.compare.model.IndexTable;
//import com.google.common.base.Function;
//import com.google.common.collect.MigrateMap;
//import com.google.common.collect.OtterMigrateMap;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.Nullable;
//import java.util.Map;
//
///**
// * Created by huaseng on 2019/8/6.
// */
//public class IndexTableFactory {
//
//    private static final Logger logger = LoggerFactory.getLogger(IndexTable.class);
//
//    private Map<Long, Map<String, IndexTable>> indexTables;
//
//    public IndexTableFactory() {
//        indexTables = OtterMigrateMap.makeSoftValueComputingMapWithRemoveListenr(
//                new Function<Long, Map<String, IndexTable>>() {
//                    public Map<String, IndexTable> apply(@Nullable Long input) {
//                        return MigrateMap.makeComputingMap(new Function<String, IndexTable>() {
//                            public IndexTable apply(@Nullable String input) {
//                                return null;
//                            }
//                        });
//                    }
//                }
//                , new OtterMigrateMap.OtterRemovalListener<Long, Map<String, IndexTable>>() {
//                    @Override
//                    public void onRemoval(Long key, Map<String, IndexTable> indexTables) {
//                        if (indexTables == null) {
//                            return;
//                        }
//                        for (IndexTable indexTable : indexTables.values()) {
//
//                        }
//                    }
//
//                }
//        );
//    }
//
//    public IndexTable getIndexTable(Long pipelineId, String name) {
//        return indexTables.get(pipelineId).get(name);
//    }
//}
