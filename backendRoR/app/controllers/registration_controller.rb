class RegistrationController < ActionController::API
    
    def registrate
        command = RegisterUser.call(params[:email], params[:password])

        if command.success?
            render json: { 
                response: "success",
                result: command.result
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :conflict
        end
    end

    def confirm_email
        user = User.find_by_confirm_token(params[:id])
        if user
            email_activate(user)
            render json: { 
                response: "success",
                result: "User activated"
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: "User not found" 
                }, status: :bad_request
        end
    end

    private

    def email_activate(user)
        user.email_confirmed = true
        user.confirm_token = nil
        user.save
    end
end