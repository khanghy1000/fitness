package com.example.fitness.data.repository;

import com.example.fitness.data.network.retrofit.ConnectionsApi;
import com.example.fitness.data.network.model.generated.*;
import com.example.fitness.data.network.model.ErrorResponse;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ConnectionsRepository {
    private final ConnectionsApi connectionsApi;
    private final Moshi moshi;

    @Inject
    public ConnectionsRepository(ConnectionsApi connectionsApi) {
        this.connectionsApi = connectionsApi;
        this.moshi = new Moshi.Builder().build();
    }

    private String parseErrorMessage(Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                String errorBodyString = errorBody.string();
                ErrorResponse errorResponse = moshi.adapter(ErrorResponse.class).fromJson(errorBodyString);
                if (errorResponse != null) {
                    String errorMessage = errorResponse.getErrorMessage();
                    if (errorMessage != null) {
                        return errorMessage;
                    }
                }
            } else {
                return "Request failed: " + response.message();
            }
        } catch (Exception e) {
            return "Request failed: " + response.message();
        }
        return "Request failed: " + response.message();
    }

    public interface ConnectionsCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void acceptConnectionRequest(String traineeId, ConnectionsCallback<ConnectionWithoutCoachTrainee> callback) {
        TraineeId request = new TraineeId(traineeId);
        connectionsApi.apiConnectionsAcceptPost(request).enqueue(new Callback<ConnectionWithoutCoachTrainee>() {
            @Override
            public void onResponse(Call<ConnectionWithoutCoachTrainee> call, Response<ConnectionWithoutCoachTrainee> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ConnectionWithoutCoachTrainee> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void sendConnectionRequest(ConnectRequest connectRequest, ConnectionsCallback<ConnectionWithoutCoachTrainee> callback) {
        connectionsApi.apiConnectionsConnectPost(connectRequest).enqueue(new Callback<ConnectionWithoutCoachTrainee>() {
            @Override
            public void onResponse(Call<ConnectionWithoutCoachTrainee> call, Response<ConnectionWithoutCoachTrainee> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ConnectionWithoutCoachTrainee> call, Throwable t) {
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
                    callback.onError(parseErrorMessage(response));
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
                    callback.onError(parseErrorMessage(response));
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
                    callback.onError(parseErrorMessage(response));
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
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * @param type 'sent' or 'received'
     */
    public void getConnectionRequests(String type, ConnectionsCallback<java.util.List<Connection>> callback) {
        connectionsApi.apiConnectionsRequestsTypeGet(type).enqueue(new Callback<java.util.List<Connection>>() {
            @Override
            public void onResponse(Call<java.util.List<Connection>> call, Response<java.util.List<Connection>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<java.util.List<Connection>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
