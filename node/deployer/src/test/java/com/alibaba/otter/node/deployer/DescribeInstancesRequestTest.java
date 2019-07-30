package com.alibaba.otter.node.deployer;

import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.BinlogDownloadQueue;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.RdsBinlogOpenApi;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.data.BinlogFile;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.data.DescribeBinlogFileResult;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.data.RdsItem;
import com.alibaba.otter.canal.parse.inbound.mysql.rds.request.DescribeBinlogFilesRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.DescribeInstancesRequest;
import com.aliyuncs.ecs.model.v20140526.DescribeInstancesResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by huahua on 2019/7/25.
 */
public class DescribeInstancesRequestTest {
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
        List<BinlogFile> binlogFiles = listBinlogFiles(url,
                accesskey,
                secretkey,
                instanceId,
               null,
                null
        );
        if (binlogFiles.isEmpty()) {
            throw new CanalParseException("start timestamp : "  + " binlog files is empty");
        }

        binlogDownloadQueue = new BinlogDownloadQueue(binlogFiles, batchFileSize, directory);
        binlogDownloadQueue.silenceDownload();
        needWait = true;
        // try to download one file,use to test server id
        binlogDownloadQueue.tryOne();

    }
    public static List<BinlogFile> listBinlogFiles(String url, String ak, String sk, String dbInstanceId,
                                                   Date startTime, Date endTime) {
        DescribeBinlogFilesRequest request = new DescribeBinlogFilesRequest();
        request.putQueryString("Action","DescribeLogBackupFiles");
        if (StringUtils.isNotEmpty(url)) {
            try {
                URI uri = new URI(url);
                request.setEndPoint(uri.getHost());
            } catch (URISyntaxException e) {
//                logger.error("resolve url host failed, will use default rds endpoint!");
            }
        }
//        request.setStartDate(startTime);
//        request.setEndDate(endTime);
        request.setPageNumber(1);
        request.setPageSize(100);
        request.setRdsInstanceId(dbInstanceId);
        request.setAccessKeyId(ak);
        request.setAccessKeySecret(sk);
        DescribeBinlogFileResult result = null;
        int retryTime = 3;
        while (true) {
            try {
                result = request.doAction();
                break;
            } catch (Exception e) {
                if (retryTime-- <= 0) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e1) {
                }
            }
        }
        if (result == null) {
            return Collections.EMPTY_LIST;
        }
        RdsItem rdsItem = result.getItems();
        if (rdsItem != null) {
            return rdsItem.getBinLogFile();
        }
        return Collections.EMPTY_LIST;
    }
}
