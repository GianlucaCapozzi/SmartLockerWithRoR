class ConfigurationController < ActionController::API
    before_action :authenticate_request

    def configure
        command = Configure
        
    end
end