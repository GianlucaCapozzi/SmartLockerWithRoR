class BlacklistingController < ApplicationController
    before_action :authenticate_request
    
    def blacklist
        command = BlacklistToken.call(request.headers)

        if command.success?
            render json: { 
                response: "success",
                auth_token: "Token blacklisted" 
            }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :bad_request
        end
    end
end