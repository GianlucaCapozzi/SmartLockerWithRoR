class DeletionController < ApplicationController
    before_action :authenticate_request

    def delete_account 
        command = DeleteAccount.call(request.headers, params[:password])

        if command.success?
            render json: { 
                response: "success",
                result: "Account deleted"
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :bad_request
        end
    end
end