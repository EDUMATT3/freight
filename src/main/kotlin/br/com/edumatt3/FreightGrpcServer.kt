package br.com.edumatt3

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
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

        val zipcode = request?.zipcode
        if (zipcode.isNullOrBlank()){
            val exception = Status.INVALID_ARGUMENT
                .withDescription("zipcode should be present")
                .asRuntimeException()

            responseObserver?.onError(exception)
        }

        if (!zipcode!!.matches("[0-9]{5}-[0-9]{3}".toRegex())){
            val exception = Status.INVALID_ARGUMENT
                .withDescription("invalid zipcode")
                .augmentDescription("expected format should be 99999-99")
                .asRuntimeException()

            responseObserver?.onError(exception)
        }

        //SIMULAR verificação de seg
        //o status de erro acima não permite objetos complexos, esse novo do google sim
        //o cliente deve estar ciente desses detalhes
        if (zipcode.endsWith("333")){
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("User cant access this feature ")
                .addDetails(Any.pack(ErrorDetails.newBuilder().setCode(401).setMessage("token expirado").build())) // precisa definir essa mensagem de erro no protobuf
                .build()

            //convert para statusruntimeexcep..

            val exception = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(exception)
        }


        var value = 0.0

        //tratar erros
        try {
            value = Random.nextDouble(from = 0.0, until = 140.0)//complexy logic
            //simula erro
            if(value > 100.0) throw IllegalArgumentException("Erro inesperado ao executar lógica de negócio!")
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)//anexado ao status, mas não é enviado ao Client
                .asRuntimeException())
        }
        val response = CalculateFreightResponse.newBuilder()
            .setZipcode(request!!.zipcode)
            .setValue(value)
            .build()

        logger.info("Calculated freight: $response")
        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}