class ConfigurationController < ApplicationController
    skip_before_action :authenticate_request                #because it uses a special token

    def configure
        command = ConfigureProfile.call(request.headers, params[:img], params[:name], params[:surname], params[:gender], params[:age], params[:weight])

        if command.success?
            render json: { 
                response: "success",
                result: "Info added"
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :bad_request
        end
        
    end
end