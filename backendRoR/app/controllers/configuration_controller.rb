class ConfigurationController < ApplicationController
    #before_action :authenticate_request        # DEBUG ONLY PLEASE UNCOMMENT IT !!!
    skip_before_action :authenticate_request    # DEBUG ONLY PLEASE DELETE IT !!!

    def configure
        command = ConfigureProfile.call(request.headers, params[:img], params[:name], params[:surname], params[:age], params[:weight])

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