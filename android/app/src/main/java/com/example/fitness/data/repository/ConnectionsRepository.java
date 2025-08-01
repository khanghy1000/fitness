package com.example.fitness.data.repository;

import com.example.fitness.data.network.retrofit.ConnectionsApi;
import com.example.fitness.data.network.model.generated.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ConnectionsRepository {
    private final ConnectionsApi connectionsApi;

    @Inject
    public ConnectionsRepository(ConnectionsApi connectionsApi) {
        this.connectionsApi = connectionsApi;
    }

    public interface ConnectionsCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void acceptConnectionRequest(String traineeId, ConnectionsCallback<Connection> callback) {
        TraineeId request = new TraineeId(traineeId);
        connectionsApi.apiConnectionsAcceptPost(request).enqueue(new Callback<Connection>() {
            @Override
            public void onResponse(Call<Connection> call, Response<Connection> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to accept connection request: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Connection> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void sendConnectionRequest(ConnectRequest connectRequest, ConnectionsCallback<Connection> callback) {
        connectionsApi.apiConnectionsConnectPost(connectRequest).enqueue(new Callback<Connection>() {
            @Override
            public void onResponse(Call<Connection> call, Response<Connection> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to send connection request: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Connection> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getActiveConnections(ConnectionsCallback<java.util.List<Connection>> callback) {
        connectionsApi.apiConnectionsActiveGet().enqueue(new Callback<java.util.List<Connection>>() {
            @Override
            public void onResponse(Call<java.util.List<Connection>> call, Response<java.util.List<Connection>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get connections: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<Connection>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAllConnections(ConnectionsCallback<java.util.List<Connection>> callback) {
        connectionsApi.apiConnectionsAllGet().enqueue(new Callback<java.util.List<Connection>>() {
            @Override
            public void onResponse(Call<java.util.List<Connection>> call, Response<java.util.List<Connection>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get connections: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<Connection>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void disconnectTrainee(String traineeId, ConnectionsCallback<SuccessMessage> callback) {
        TraineeId request = new TraineeId(traineeId);
        connectionsApi.apiConnectionsDisconnectPost(request).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to disconnect trainee: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void rejectConnectionRequest(String traineeId, ConnectionsCallback<SuccessMessage> callback) {
        TraineeId request = new TraineeId(traineeId);
        connectionsApi.apiConnectionsRejectPost(request).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to reject connection request: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getConnectionRequests(String type, ConnectionsCallback<java.util.List<Connection>> callback) {
        connectionsApi.apiConnectionsRequestsTypeGet(type).enqueue(new Callback<java.util.List<Connection>>() {
            @Override
            public void onResponse(Call<java.util.List<Connection>> call, Response<java.util.List<Connection>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get connection requests: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<Connection>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
