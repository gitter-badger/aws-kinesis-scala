package jp.co.bizreach

import java.nio.ByteBuffer
import scala.collection.JavaConverters._

import com.amazonaws.services.kinesis.model.{AddTagsToStreamRequest => AWSAddTagsToStreamRequest}
import com.amazonaws.services.kinesis.model.{CreateStreamRequest => AWSCreateStreamRequest}
import com.amazonaws.services.kinesis.model.{DeleteStreamRequest => AWSDeleteStreamRequest}
import com.amazonaws.services.kinesis.model.{DescribeStreamRequest => AWSDescribeStreamRequest}
import com.amazonaws.services.kinesis.model.{DescribeStreamResult => AWSDescribeStreamResult}
import com.amazonaws.services.kinesis.model.{GetShardIteratorRequest => AWSGetShardIteratorRequest}
import com.amazonaws.services.kinesis.model.{GetShardIteratorResult => AWSGetShardIteratorResult}
import com.amazonaws.services.kinesis.model.{GetRecordsRequest => AWSGetRecordsRequest}
import com.amazonaws.services.kinesis.model.{GetRecordsResult => AWSGetRecordsResult}
import com.amazonaws.services.kinesis.model.{ListStreamsRequest => AWSListStreamsRequest}
import com.amazonaws.services.kinesis.model.{ListStreamsResult => AWSListStreamsResult}
import com.amazonaws.services.kinesis.model.{ListTagsForStreamRequest => AWSListTagsForStreamRequest}
import com.amazonaws.services.kinesis.model.{ListTagsForStreamResult => AWSListTagsForStreamResult}
import com.amazonaws.services.kinesis.model.{MergeShardsRequest => AWSMergeShardsRequest}
import com.amazonaws.services.kinesis.model.{PutRecordRequest => AWSPutRecordRequest}
import com.amazonaws.services.kinesis.model.{PutRecordResult => AWSPutRecordResult}
import com.amazonaws.services.kinesis.model.{PutRecordsRequest => AWSPutRecordsRequest}
import com.amazonaws.services.kinesis.model.{PutRecordsResult => AWSPutRecordsResult}
import com.amazonaws.services.kinesis.model.{PutRecordsRequestEntry => AWSPutRecordsRequestEntry}
import com.amazonaws.services.kinesis.model.{RemoveTagsFromStreamRequest => AWSRemoveTagsFromStreamRequest}
import com.amazonaws.services.kinesis.model.{SplitShardRequest => AWSSplitShardRequest}

import scala.language.implicitConversions

package object kinesis {

  type AmazonKinesisClient = com.amazonaws.services.kinesis.AmazonKinesisClient

  case class AddTagsToStreamRequest(streamName: String, tags: Map[String, String])

  implicit def convertAddTagsToStreamRequest(request: AddTagsToStreamRequest): AWSAddTagsToStreamRequest = {
    val awsRequest = new AWSAddTagsToStreamRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setTags(request.tags.asJava)
    awsRequest
  }

  case class CreateStreamRequest(streamName: String, shardCount: Int)

  implicit def convertCreateStreamRequest(request: CreateStreamRequest): AWSCreateStreamRequest = {
    val awsRequest = new AWSCreateStreamRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setShardCount(request.shardCount)
    awsRequest
  }

  case class DeleteStreamRequest(streamName: String)

  implicit def convertDeleteStreamRequest(request: DeleteStreamRequest): AWSDeleteStreamRequest = {
    val awsRequest = new AWSDeleteStreamRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest
  }

  case class DescribeStreamRequest(streamName: String, limit: Option[Int] = None, exclusiveStartShardId: Option[String] = None)

  implicit def convertDescribeStreamRequest(request: DescribeStreamRequest): AWSDescribeStreamRequest = {
    val awsRequest = new AWSDescribeStreamRequest()
    awsRequest.setStreamName(request.streamName)
    request.limit.foreach { limit =>
      awsRequest.setLimit(limit)
    }
    request.exclusiveStartShardId.foreach { exclusiveStartShardId =>
      awsRequest.setExclusiveStartShardId(exclusiveStartShardId)
    }
    awsRequest
  }

  case class DescribeStreamResult(streamDescription: StreamDescription)
  case class StreamDescription(streamName: String, streamARN: String, streamStatus: String, shards: Seq[Shard], hasMoreShards: Boolean)
  case class Shard(shardId: String, parentShardId: String, adjacentParentShardId: String, hashKeyRange: HashKeyRange, sequenceNumberRange: SequenceNumberRange)
  case class HashKeyRange(startingHashKey: String, endingHashKey: String) // TODO BitInt?
  case class SequenceNumberRange(startingSequenceNumber: String, endingSequenceNumber: String) // TODO BigInt?

  implicit def convertDescribeStreamResult(result: AWSDescribeStreamResult): DescribeStreamResult = {
    DescribeStreamResult(
      StreamDescription(
        streamName = result.getStreamDescription.getStreamName,
        streamARN = result.getStreamDescription.getStreamARN,
        streamStatus = result.getStreamDescription.getStreamStatus,
        shards = result.getStreamDescription.getShards.asScala.map { shard =>
          Shard(
            shardId = shard.getShardId,
            parentShardId = shard.getParentShardId,
            adjacentParentShardId = shard.getAdjacentParentShardId,
            hashKeyRange = HashKeyRange(
              startingHashKey = shard.getHashKeyRange.getStartingHashKey,
              endingHashKey = shard.getHashKeyRange.getEndingHashKey
            ),
            sequenceNumberRange = SequenceNumberRange(
              startingSequenceNumber = shard.getSequenceNumberRange.getStartingSequenceNumber,
              endingSequenceNumber = shard.getSequenceNumberRange.getEndingSequenceNumber
            )
          )
        },
        hasMoreShards = result.getStreamDescription.getHasMoreShards
      )
    )
  }

  case class GetRecordsRequest(shardIterator: String, limit: Option[Int] = None)

  implicit def convertGetRecordsRequest(request: GetRecordsRequest): AWSGetRecordsRequest = {
    val awsRequest = new AWSGetRecordsRequest()
    awsRequest.setShardIterator(request.shardIterator)
    request.limit.foreach { limit =>
      awsRequest.setLimit(limit)
    }
    awsRequest
  }

  case class GetRecordsResult(records: Seq[Record], nextShardIterator: String, millisBehindLatest: Long)
  case class Record(data: Array[Byte], sequenceNumber: String, partitionKey: String)

  implicit def convertGetRecordsResult(result: AWSGetRecordsResult): GetRecordsResult = {
    GetRecordsResult(
      records = result.getRecords.asScala.map { record =>
        Record(
          data           = record.getData.array(),
          sequenceNumber = record.getSequenceNumber,
          partitionKey   = record.getPartitionKey
        )
      },
      nextShardIterator = result.getNextShardIterator,
      millisBehindLatest = result.getMillisBehindLatest
    )
  }

  case class GetShardIteratorRequest(streamName: String, shardId: String, shardIteratorType: Option[String] = None)

  implicit def convertGetShardIteratorRequest(request: GetShardIteratorRequest): AWSGetShardIteratorRequest = {
    val awsRequest = new AWSGetShardIteratorRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setShardId(request.shardId)
    request.shardIteratorType.foreach { shardIteratorType =>
      awsRequest.setShardIteratorType(shardIteratorType)
    }
    awsRequest
  }

  case class GetShardIteratorResult(shardIterator: String)

  implicit def convertGetShardIteratorResult(result: AWSGetShardIteratorResult): GetShardIteratorResult = {
    GetShardIteratorResult(result.getShardIterator)
  }

  case class ListStreamsRequest(limit: Option[Int] = None, exclusiveStartStreamName: Option[String] = None)

  implicit def convertListStreamsRequest(request: ListStreamsRequest): AWSListStreamsRequest = {
    val awsRequest = new AWSListStreamsRequest()
    request.limit.foreach { limit =>
      awsRequest.setLimit(limit)
    }
    request.exclusiveStartStreamName.foreach { exclusiveStartStreamName =>
      awsRequest.setExclusiveStartStreamName(exclusiveStartStreamName)
    }
    awsRequest
  }

  case class ListStreamsResult(streamNames: Seq[String], hasMoreStreams: Boolean)

  implicit def convertListStreamsResult(request: AWSListStreamsResult): ListStreamsResult = {
    ListStreamsResult(
      streamNames = request.getStreamNames.asScala,
      hasMoreStreams = request.getHasMoreStreams
    )
  }

  case class ListTagsForStreamRequest(streamName: String, exclusiveStartTagKey: Option[String] = None, limit: Option[Int] = None)

  implicit def convertListTagsForStreamRequest(request: ListTagsForStreamRequest): AWSListTagsForStreamRequest = {
    val awsRequest = new AWSListTagsForStreamRequest()
    awsRequest.setStreamName(request.streamName)
    request.exclusiveStartTagKey.foreach { exclusiveStartTagKey =>
      awsRequest.setExclusiveStartTagKey(exclusiveStartTagKey)
    }
    request.limit.foreach { limit => 
      awsRequest.setLimit(limit)
    }
    awsRequest
  }

  case class ListTagsForStreamResult(tags: Seq[Tag], hasMoreTags: Boolean)
  case class Tag(key: String, value: String)

  implicit def convertListTagsForStreamResult(result: AWSListTagsForStreamResult): ListTagsForStreamResult = {
    ListTagsForStreamResult(
      tags = result.getTags.asScala.map { tag =>
        Tag(key = tag.getKey, value = tag.getValue)
      },
      hasMoreTags = result.getHasMoreTags
    )
  }

  case class MergeShardsRequest(streamName: String, shardToMerge: String, adjacentShardToMerge: String)

  implicit def convertMergeShardsRequest(request: MergeShardsRequest): AWSMergeShardsRequest = {
    val awsRequest = new AWSMergeShardsRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setShardToMerge(request.shardToMerge)
    awsRequest.setAdjacentShardToMerge(request.adjacentShardToMerge)
    awsRequest
  }

  case class PutRecordRequest(streamName: String, partitionKey: String, data: Array[Byte], explicitHashKey: Option[String] = None, sequenceNumberForOrdering: Option[String] = None)

  implicit def convertPutRecordRequest(request: PutRecordRequest): AWSPutRecordRequest = {
    val awsRequest = new AWSPutRecordRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setData(ByteBuffer.wrap(request.data))
    awsRequest.setPartitionKey(request.partitionKey)
    request.explicitHashKey.foreach { explicitHashKey =>
      awsRequest.setExplicitHashKey(explicitHashKey)
    }
    request.sequenceNumberForOrdering.foreach { sequenceNumberForOrdering =>
      awsRequest.setSequenceNumberForOrdering(sequenceNumberForOrdering)
    }
    awsRequest
  }

  case class PutRecordResult(shardId: String, sequenceNumber: String)

  implicit def convertPutRecordResult(result: AWSPutRecordResult): PutRecordResult = {
    PutRecordResult(
      shardId = result.getShardId,
      sequenceNumber = result.getSequenceNumber
    )
  }

  case class PutRecordsRequest(streamName: String, records: Seq[PutRecordsEntry])
  case class PutRecordsEntry(partitionKey: String, data: Array[Byte], explicitHashKey: Option[String] = None)

  implicit def convertPutRecordsRequest(request: PutRecordsRequest): AWSPutRecordsRequest = {
    val entries = request.records.map { entry =>
      val awsEntry = new AWSPutRecordsRequestEntry()
      awsEntry.setPartitionKey(entry.partitionKey)
      awsEntry.setData(ByteBuffer.wrap(entry.data))
      entry.explicitHashKey.foreach { explicitHashKey => 
        awsEntry.setExplicitHashKey(explicitHashKey)
      }
      awsEntry
    }

    val awsRequest = new AWSPutRecordsRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setRecords(entries.asJava)
    awsRequest
  }

  case class PutRecordsResult(failedRecordCount: Int, records: Seq[PutRecordsResultEntry])
  case class PutRecordsResultEntry(sequenceNumber: String, shardId: String, errorCode: String, errorMessage: String)

  implicit def convertPutRecordsResult(result: AWSPutRecordsResult): PutRecordsResult = {
    PutRecordsResult(
      failedRecordCount = result.getFailedRecordCount,
      records = result.getRecords.asScala.map { record =>
        PutRecordsResultEntry(
          sequenceNumber = record.getSequenceNumber,
          shardId = record.getShardId,
          errorCode = record.getErrorCode,
          errorMessage = record.getErrorMessage
        )
      }
    )
  }

  case class RemoveTagsFromStreamRequest(streamName: String, tagKeys: Seq[String])

  implicit def convertRemoveTagsFromStreamRequest(request: RemoveTagsFromStreamRequest): AWSRemoveTagsFromStreamRequest = {
    val awsRequest = new AWSRemoveTagsFromStreamRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setTagKeys(request.tagKeys.asJava)
    awsRequest
  }

  case class SplitShardRequest(streamName: String, shardToSplit: String, newStartingHashKey: String)

  implicit def convertSplitShardRequest(request: SplitShardRequest): AWSSplitShardRequest = {
    val awsRequest = new AWSSplitShardRequest()
    awsRequest.setStreamName(request.streamName)
    awsRequest.setShardToSplit(request.shardToSplit)
    awsRequest.setNewStartingHashKey(request.newStartingHashKey)
    awsRequest
  }

}
