class GetinfoController < ApplicationController
    before_action :authenticate_request
    
    def getinfo
        command = GetInfo.call(request.headers)

        if command.success?
            user = command.result
            render json: { 
                response: "success",
                email: user.email,
                name: user.name,
                surname: user.surname,
                age: user.age,
                weight: user.weight.to_f,
                photo: user.img
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :bad_request
        end
    end
end