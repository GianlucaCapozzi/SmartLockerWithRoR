class RegistrationController < ActionController::API
    
    def registrate
        command = RegisterUser.call(params[:username], params[:email], params[:password])

        if command.success?
            render json: { result: 'User created'}, status: :ok
        else
            render json: { error: command.errors }, status: :conflict
        end
    end
end