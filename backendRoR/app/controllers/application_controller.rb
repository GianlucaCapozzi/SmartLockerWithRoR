class ApplicationController < ActionController::API
    before_action :authenticate_request
    attr_reader :current_user

    private

    def authenticate_request
        call = AuthorizeApiRequest.call(request.headers)
        @current_user = call.result
        
        render json: { 
            response: "failure",
            error: call.errors
            }, status: 401 unless @current_user
    end
end
