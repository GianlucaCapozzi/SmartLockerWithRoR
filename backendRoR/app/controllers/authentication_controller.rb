class AuthenticationController < ApplicationController
    skip_before_action :authenticate_request
   
    def authenticate
        command = AuthenticateUser.call(params[:email], params[:password])

        if command.success?
            render json: { 
                response: "success",
                auth_token: command.result 
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :unauthorized
        end
    end
end