class AuthenticationController < ApplicationController
    skip_before_action :authenticate_request
   
    def authenticate
        command = AuthenticateUser.call(params[:email], params[:password])

        if command.success?
            user_info = User.find_by_email(params[:email])
            render json: { 
                response: "success",
                auth_token: command.result,
                email: user_info.email,
                name: user_info.name,
                surname: user_info.surname,
                gender: user_info.gender,
                photo: user_info.img
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors,
                conf_token: command.result  #nil if credentials not valid
                }, status: :unauthorized
        end
    end

end