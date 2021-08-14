package br.com.edumatt3

import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreightGrpcServer : FreightServiceGrpc.FreightServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FreightGrpcServer::class.java)

    override fun calculateFreight(
        request: CalculateFreightRequest?,
        responseObserver: StreamObserver<CalculateFreightResponse>?
    ) {
        logger.info("Calculating freight for request: $request")

        val response = CalculateFreightResponse.newBuilder()
            .setZipcode(request!!.zipcode)
            .setValue(Random.nextDouble(from = 0.0, until = 140.0))
            .build()

        logger.info("Calculated freight: $response")
        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}