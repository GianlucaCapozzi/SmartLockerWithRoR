class RecoveryController < ActionController::API

    def forget
        command = RecoverPassword.call(params[:email])

        if not command.success?
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :bad_request
        end
    end

    def change_email
        check = check_token
        if check
            user = User.find(decoded_auth_token[:user_id])
            if user
                old_mail = user.email
                change(user)
                UserMailer.email_changed(old_mail, user).deliver
                render json: { 
                    response: "success",
                    result: "Email changed"
                    }, status: :ok
            else
                render json: { 
                    response: "failure",
                    error: "User not found"
                    }, status: :bad_request
            end
        else
            render json: { 
                response: "failure",
                error: check.errors 
                }, status: :bad_request
        end
    end

    def change_pass_recovered
        user = User.find_by_temp_pass(params[:temp_pass])
        if user and user.reset_pass
            change(user)
            UserMailer.password_changed(user).deliver
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

    def change_pass
        check = check_token
        if check
            user = User.find(decoded_auth_token[:user_id])
            if user and user.authenticate(params[:old_pass])
                change(user)
                UserMailer.password_changed(user).deliver
                render json: { 
                    response: "success",
                    result: "Password changed"
                    }, status: :ok
            else
                render json: { 
                    response: "failure",
                    error: "Old password not correct"
                    }, status: :bad_request
            end
        else
            render json: { 
                response: "failure",
                error: check.errors 
                }, status: :bad_request
        end
    end


    private
    def change(user)
        user.email = params[:new_mail]          if params[:new_mail] != nil

        user.password = params[:new_pass]       if params[:new_pass] != nil
        user.temp_pass = nil                    if params[:new_pass] != nil
        user.reset_pass = false                 if params[:new_pass] != nil
        
        user.save
    end

    def check_token
        
        if decoded_auth_token.nil?
            errors.add(:token, 'Invalid token')
            return false
        end

        @decoded_token = decoded_auth_token
        if @decoded_token[:exp] < Time.now.to_i
            errors.add(:token, 'Token expired, please login again')
            return false
        elsif BlacklistedToken.exists?(token: http_auth_header)
            errors.add(:token, 'Token in blacklist, please login again')
            return false
        else
            return true
        end
    end

    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken::decode(http_auth_header)
    end
  
    def http_auth_header

        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end

end