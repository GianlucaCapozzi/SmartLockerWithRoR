class RecoveryController < ActionController::API

    def forget
        command = RecoverPassword.call(params[:email])

        if not command.success?
            render json: { error: command.errors }, status: :bad_request
        end
    end

    def change_pass
        user = User.find_by_temp_pass(params[:temp_pass])
        if user and user.reset_pass
            change(user)
            render json: { 
                response: "success",
                result: "Password changed"
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: "User not found" 
                }, status: :bad_request
        end
    end

    private
    def change(user)
        user.password = params[:new_pass]
        user.temp_pass = nil
        user.reset_pass = false
        user.save
    end

end