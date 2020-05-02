class OauthController < ApplicationController
    skip_before_action :authenticate_request

    def auth_oauth
        #try to signup
        res = check_token
        if res == "Not exists"
            command = RegisterUserOauth.call(params[:email], params[:token_oauth])
    
            if command.success?
                render json: { 
                    response: "success",
                    type: "signup",
                    conf_token: command.result
                    }, status: :ok
            else
                render json: { 
                    response: "failure",
                    type: "signup",
                    error: command.errors 
                    }, status: :conflict
            end
        #try to login
        elsif res == "Exists"
            command = AuthOauthUser.call(params[:email], params[:token_oauth])
            
            if command.success?
                user_info = User.find(command.result)
                token = JsonWebToken::encode(user_id: command.result)
                render json: { 
                    response: "success",
                    type: "login",
                    auth_token: token,
                    email: user_info.email,
                    name: user_info.name,
                    surname: user_info.surname,
                    gender: user_info.gender,
                    photo: user_info.img
                    }, status: :ok
            else
                render json: { 
                    response: "failure",
                    type: "login",
                    error: command.errors,
                    conf_token: command.result
                    }, status: :unauthorized
            end
        else
            render json: { 
                response: "failure",
                error: "Token not valid" 
                }, status: :bad_request
        end
    end

    private

    attr_accessor :token_oauth

    # Function to check if there is an user with user_id equalt to token's one 
    def check_token
        id_oauth = get_id_oauth
        if not id_oauth.nil?
            if User.exists?(id_oauth: id_oauth)
                return "Exists"
            else
                return "Not exists" 
            end
        else
            return "Error"
        end
    end

    # Function to check if the oauth token is valid
    def get_id_oauth
        response = HTTParty.get('https://graph.facebook.com/debug_token?input_token='+params[:token_oauth]+'&access_token='+ENV["APP_ID_FACEBOOK"]+'|'+ENV["SECRET_KEY_FACEBOOK"])
        if response.code == 200
            body = JSON.parse(response.body)
            return body['data']['user_id'] if body['data']['is_valid']
        end
        nil
    end
end