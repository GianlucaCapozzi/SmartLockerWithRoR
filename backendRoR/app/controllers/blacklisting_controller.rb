class BlacklistingController < ApplicationController
    before_action :authenticate_request
    
    def blacklist
        command = BlacklistToken.call(request.headers)

        if command.success?
            render json: { auth_token: 'Token blacklisted' }
        else
            render json: { error: command.errors }, status: :bad_request
        end
    end
end