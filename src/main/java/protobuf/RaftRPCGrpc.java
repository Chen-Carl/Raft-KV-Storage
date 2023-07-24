package protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.56.0)",
    comments = "Source: raft.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RaftRPCGrpc {

  private RaftRPCGrpc() {}

  public static final String SERVICE_NAME = "raftrpc.RaftRPC";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<protobuf.RaftRPCProto.RequestVoteRequest,
      protobuf.RaftRPCProto.RequestVoteResponse> getRequestVoteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RequestVote",
      requestType = protobuf.RaftRPCProto.RequestVoteRequest.class,
      responseType = protobuf.RaftRPCProto.RequestVoteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.RaftRPCProto.RequestVoteRequest,
      protobuf.RaftRPCProto.RequestVoteResponse> getRequestVoteMethod() {
    io.grpc.MethodDescriptor<protobuf.RaftRPCProto.RequestVoteRequest, protobuf.RaftRPCProto.RequestVoteResponse> getRequestVoteMethod;
    if ((getRequestVoteMethod = RaftRPCGrpc.getRequestVoteMethod) == null) {
      synchronized (RaftRPCGrpc.class) {
        if ((getRequestVoteMethod = RaftRPCGrpc.getRequestVoteMethod) == null) {
          RaftRPCGrpc.getRequestVoteMethod = getRequestVoteMethod =
              io.grpc.MethodDescriptor.<protobuf.RaftRPCProto.RequestVoteRequest, protobuf.RaftRPCProto.RequestVoteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RequestVote"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.RaftRPCProto.RequestVoteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.RaftRPCProto.RequestVoteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new RaftRPCMethodDescriptorSupplier("RequestVote"))
              .build();
        }
      }
    }
    return getRequestVoteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<protobuf.RaftRPCProto.AppendEntriesRequest,
      protobuf.RaftRPCProto.AppendEntriesResponse> getAppendEntriesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AppendEntries",
      requestType = protobuf.RaftRPCProto.AppendEntriesRequest.class,
      responseType = protobuf.RaftRPCProto.AppendEntriesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.RaftRPCProto.AppendEntriesRequest,
      protobuf.RaftRPCProto.AppendEntriesResponse> getAppendEntriesMethod() {
    io.grpc.MethodDescriptor<protobuf.RaftRPCProto.AppendEntriesRequest, protobuf.RaftRPCProto.AppendEntriesResponse> getAppendEntriesMethod;
    if ((getAppendEntriesMethod = RaftRPCGrpc.getAppendEntriesMethod) == null) {
      synchronized (RaftRPCGrpc.class) {
        if ((getAppendEntriesMethod = RaftRPCGrpc.getAppendEntriesMethod) == null) {
          RaftRPCGrpc.getAppendEntriesMethod = getAppendEntriesMethod =
              io.grpc.MethodDescriptor.<protobuf.RaftRPCProto.AppendEntriesRequest, protobuf.RaftRPCProto.AppendEntriesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AppendEntries"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.RaftRPCProto.AppendEntriesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.RaftRPCProto.AppendEntriesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new RaftRPCMethodDescriptorSupplier("AppendEntries"))
              .build();
        }
      }
    }
    return getAppendEntriesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RaftRPCStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RaftRPCStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RaftRPCStub>() {
        @java.lang.Override
        public RaftRPCStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RaftRPCStub(channel, callOptions);
        }
      };
    return RaftRPCStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RaftRPCBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RaftRPCBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RaftRPCBlockingStub>() {
        @java.lang.Override
        public RaftRPCBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RaftRPCBlockingStub(channel, callOptions);
        }
      };
    return RaftRPCBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RaftRPCFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RaftRPCFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RaftRPCFutureStub>() {
        @java.lang.Override
        public RaftRPCFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RaftRPCFutureStub(channel, callOptions);
        }
      };
    return RaftRPCFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void requestVote(protobuf.RaftRPCProto.RequestVoteRequest request,
        io.grpc.stub.StreamObserver<protobuf.RaftRPCProto.RequestVoteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRequestVoteMethod(), responseObserver);
    }

    /**
     * <pre>
     * rpc InstallSnapshot (InstallSnapshotRequest) returns (InstallSnapshotResponse) {}
     * </pre>
     */
    default void appendEntries(protobuf.RaftRPCProto.AppendEntriesRequest request,
        io.grpc.stub.StreamObserver<protobuf.RaftRPCProto.AppendEntriesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAppendEntriesMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service RaftRPC.
   */
  public static abstract class RaftRPCImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return RaftRPCGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service RaftRPC.
   */
  public static final class RaftRPCStub
      extends io.grpc.stub.AbstractAsyncStub<RaftRPCStub> {
    private RaftRPCStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftRPCStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RaftRPCStub(channel, callOptions);
    }

    /**
     */
    public void requestVote(protobuf.RaftRPCProto.RequestVoteRequest request,
        io.grpc.stub.StreamObserver<protobuf.RaftRPCProto.RequestVoteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRequestVoteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * rpc InstallSnapshot (InstallSnapshotRequest) returns (InstallSnapshotResponse) {}
     * </pre>
     */
    public void appendEntries(protobuf.RaftRPCProto.AppendEntriesRequest request,
        io.grpc.stub.StreamObserver<protobuf.RaftRPCProto.AppendEntriesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service RaftRPC.
   */
  public static final class RaftRPCBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<RaftRPCBlockingStub> {
    private RaftRPCBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftRPCBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RaftRPCBlockingStub(channel, callOptions);
    }

    /**
     */
    public protobuf.RaftRPCProto.RequestVoteResponse requestVote(protobuf.RaftRPCProto.RequestVoteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRequestVoteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * rpc InstallSnapshot (InstallSnapshotRequest) returns (InstallSnapshotResponse) {}
     * </pre>
     */
    public protobuf.RaftRPCProto.AppendEntriesResponse appendEntries(protobuf.RaftRPCProto.AppendEntriesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAppendEntriesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service RaftRPC.
   */
  public static final class RaftRPCFutureStub
      extends io.grpc.stub.AbstractFutureStub<RaftRPCFutureStub> {
    private RaftRPCFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftRPCFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RaftRPCFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.RaftRPCProto.RequestVoteResponse> requestVote(
        protobuf.RaftRPCProto.RequestVoteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRequestVoteMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * rpc InstallSnapshot (InstallSnapshotRequest) returns (InstallSnapshotResponse) {}
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.RaftRPCProto.AppendEntriesResponse> appendEntries(
        protobuf.RaftRPCProto.AppendEntriesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST_VOTE = 0;
  private static final int METHODID_APPEND_ENTRIES = 1;

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
        case METHODID_REQUEST_VOTE:
          serviceImpl.requestVote((protobuf.RaftRPCProto.RequestVoteRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.RaftRPCProto.RequestVoteResponse>) responseObserver);
          break;
        case METHODID_APPEND_ENTRIES:
          serviceImpl.appendEntries((protobuf.RaftRPCProto.AppendEntriesRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.RaftRPCProto.AppendEntriesResponse>) responseObserver);
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
          getRequestVoteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.RaftRPCProto.RequestVoteRequest,
              protobuf.RaftRPCProto.RequestVoteResponse>(
                service, METHODID_REQUEST_VOTE)))
        .addMethod(
          getAppendEntriesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.RaftRPCProto.AppendEntriesRequest,
              protobuf.RaftRPCProto.AppendEntriesResponse>(
                service, METHODID_APPEND_ENTRIES)))
        .build();
  }

  private static abstract class RaftRPCBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RaftRPCBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return protobuf.RaftRPCProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("RaftRPC");
    }
  }

  private static final class RaftRPCFileDescriptorSupplier
      extends RaftRPCBaseDescriptorSupplier {
    RaftRPCFileDescriptorSupplier() {}
  }

  private static final class RaftRPCMethodDescriptorSupplier
      extends RaftRPCBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RaftRPCMethodDescriptorSupplier(String methodName) {
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
      synchronized (RaftRPCGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RaftRPCFileDescriptorSupplier())
              .addMethod(getRequestVoteMethod())
              .addMethod(getAppendEntriesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
