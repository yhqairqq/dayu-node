package com.alibaba.otter.node.deployer;

import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.BinlogDownloadQueue;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.RdsBinlogOpenApi;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.data.BinlogFile;

import java.util.Date;
import java.util.List;

/**
 * Created by huahua on 2019/7/25.
 */
public class BinlogDownloadTest {
    public static void main(String[] args) throws Throwable {
         String              url = "https://rds.aliyuncs.com/";                // openapi地址
         String              accesskey = "LTAIPe07SPDoVMS2";          // 云账号的ak
         String              secretkey ="xjWJ9HpcKhyZCpgrTS2vF5hwKe5TPG";          // 云账号sk
         String              instanceId = "rm-bp10p2vl5x3c4mum5";         // rds实例id
         Long                startTime = 1564027261000L;
         Long                endTime =   1564038061000L;
         boolean            needWait   = false;
        int                 batchFileSize = 1000;
        String directory = "/Users/YHQ/Downloads/binlog";

         BinlogDownloadQueue binlogDownloadQueue;
        List<BinlogFile> binlogFiles = RdsBinlogOpenApi.listBinlogFiles(url,
                accesskey,
                secretkey,
                instanceId,
                new Date(startTime),
                new Date(endTime));
        if (binlogFiles.isEmpty()) {
            throw new CanalParseException("start timestamp : "  + " binlog files is empty");
        }

        binlogDownloadQueue = new BinlogDownloadQueue(binlogFiles, batchFileSize, directory);
        binlogDownloadQueue.silenceDownload();
        needWait = true;
        // try to download one file,use to test server id
        binlogDownloadQueue.tryOne();
    }
}
