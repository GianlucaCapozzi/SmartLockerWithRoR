class WakeupController < ApplicationController
    skip_before_action :authenticate_request

    def wakeup
        render json: { 
            response: "success"
            }, status: :ok
    end
end