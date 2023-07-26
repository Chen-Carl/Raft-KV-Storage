package protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.56.0)",
    comments = "Source: kvstorage.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class KvStorageGrpc {

  private KvStorageGrpc() {}

  public static final String SERVICE_NAME = "raftrpc.KvStorage";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<protobuf.KvStorageProto.GetRequest,
      protobuf.KvStorageProto.GetResponse> getGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Get",
      requestType = protobuf.KvStorageProto.GetRequest.class,
      responseType = protobuf.KvStorageProto.GetResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.KvStorageProto.GetRequest,
      protobuf.KvStorageProto.GetResponse> getGetMethod() {
    io.grpc.MethodDescriptor<protobuf.KvStorageProto.GetRequest, protobuf.KvStorageProto.GetResponse> getGetMethod;
    if ((getGetMethod = KvStorageGrpc.getGetMethod) == null) {
      synchronized (KvStorageGrpc.class) {
        if ((getGetMethod = KvStorageGrpc.getGetMethod) == null) {
          KvStorageGrpc.getGetMethod = getGetMethod =
              io.grpc.MethodDescriptor.<protobuf.KvStorageProto.GetRequest, protobuf.KvStorageProto.GetResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Get"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.KvStorageProto.GetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.KvStorageProto.GetResponse.getDefaultInstance()))
              .setSchemaDescriptor(new KvStorageMethodDescriptorSupplier("Get"))
              .build();
        }
      }
    }
    return getGetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<protobuf.KvStorageProto.SetRequest,
      protobuf.KvStorageProto.SetResponse> getSetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Set",
      requestType = protobuf.KvStorageProto.SetRequest.class,
      responseType = protobuf.KvStorageProto.SetResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.KvStorageProto.SetRequest,
      protobuf.KvStorageProto.SetResponse> getSetMethod() {
    io.grpc.MethodDescriptor<protobuf.KvStorageProto.SetRequest, protobuf.KvStorageProto.SetResponse> getSetMethod;
    if ((getSetMethod = KvStorageGrpc.getSetMethod) == null) {
      synchronized (KvStorageGrpc.class) {
        if ((getSetMethod = KvStorageGrpc.getSetMethod) == null) {
          KvStorageGrpc.getSetMethod = getSetMethod =
              io.grpc.MethodDescriptor.<protobuf.KvStorageProto.SetRequest, protobuf.KvStorageProto.SetResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Set"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.KvStorageProto.SetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.KvStorageProto.SetResponse.getDefaultInstance()))
              .setSchemaDescriptor(new KvStorageMethodDescriptorSupplier("Set"))
              .build();
        }
      }
    }
    return getSetMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static KvStorageStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<KvStorageStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<KvStorageStub>() {
        @java.lang.Override
        public KvStorageStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new KvStorageStub(channel, callOptions);
        }
      };
    return KvStorageStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static KvStorageBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<KvStorageBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<KvStorageBlockingStub>() {
        @java.lang.Override
        public KvStorageBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new KvStorageBlockingStub(channel, callOptions);
        }
      };
    return KvStorageBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static KvStorageFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<KvStorageFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<KvStorageFutureStub>() {
        @java.lang.Override
        public KvStorageFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new KvStorageFutureStub(channel, callOptions);
        }
      };
    return KvStorageFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void get(protobuf.KvStorageProto.GetRequest request,
        io.grpc.stub.StreamObserver<protobuf.KvStorageProto.GetResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetMethod(), responseObserver);
    }

    /**
     */
    default void set(protobuf.KvStorageProto.SetRequest request,
        io.grpc.stub.StreamObserver<protobuf.KvStorageProto.SetResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service KvStorage.
   */
  public static abstract class KvStorageImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return KvStorageGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service KvStorage.
   */
  public static final class KvStorageStub
      extends io.grpc.stub.AbstractAsyncStub<KvStorageStub> {
    private KvStorageStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KvStorageStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new KvStorageStub(channel, callOptions);
    }

    /**
     */
    public void get(protobuf.KvStorageProto.GetRequest request,
        io.grpc.stub.StreamObserver<protobuf.KvStorageProto.GetResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void set(protobuf.KvStorageProto.SetRequest request,
        io.grpc.stub.StreamObserver<protobuf.KvStorageProto.SetResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service KvStorage.
   */
  public static final class KvStorageBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<KvStorageBlockingStub> {
    private KvStorageBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KvStorageBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new KvStorageBlockingStub(channel, callOptions);
    }

    /**
     */
    public protobuf.KvStorageProto.GetResponse get(protobuf.KvStorageProto.GetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetMethod(), getCallOptions(), request);
    }

    /**
     */
    public protobuf.KvStorageProto.SetResponse set(protobuf.KvStorageProto.SetRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service KvStorage.
   */
  public static final class KvStorageFutureStub
      extends io.grpc.stub.AbstractFutureStub<KvStorageFutureStub> {
    private KvStorageFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KvStorageFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new KvStorageFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.KvStorageProto.GetResponse> get(
        protobuf.KvStorageProto.GetRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.KvStorageProto.SetResponse> set(
        protobuf.KvStorageProto.SetRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET = 0;
  private static final int METHODID_SET = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET:
          serviceImpl.get((protobuf.KvStorageProto.GetRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.KvStorageProto.GetResponse>) responseObserver);
          break;
        case METHODID_SET:
          serviceImpl.set((protobuf.KvStorageProto.SetRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.KvStorageProto.SetResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.KvStorageProto.GetRequest,
              protobuf.KvStorageProto.GetResponse>(
                service, METHODID_GET)))
        .addMethod(
          getSetMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.KvStorageProto.SetRequest,
              protobuf.KvStorageProto.SetResponse>(
                service, METHODID_SET)))
        .build();
  }

  private static abstract class KvStorageBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    KvStorageBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return protobuf.KvStorageProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("KvStorage");
    }
  }

  private static final class KvStorageFileDescriptorSupplier
      extends KvStorageBaseDescriptorSupplier {
    KvStorageFileDescriptorSupplier() {}
  }

  private static final class KvStorageMethodDescriptorSupplier
      extends KvStorageBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    KvStorageMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (KvStorageGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new KvStorageFileDescriptorSupplier())
              .addMethod(getGetMethod())
              .addMethod(getSetMethod())
              .build();
        }
      }
    }
    return result;
  }
}
