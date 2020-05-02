class RecoveryController < ActionController::API

    def forget
        command = RecoverPassword.call(params[:email])

        if command.success?
            render json: { 
                response: "success",
                result: "Mail sent"
                }, status: :ok
        else
            render json: { 
                response: "failure",
                error: command.errors 
                }, status: :bad_request
        end
    end

    def change_email
        check = AuthorizeApiRequest.call(request.headers)
        if check.success?
            user = User.find(check.result.id)
            if user
                if user.oauth == nil or not user.oauth
                    if not ( params[:new_email] and User.exists?(email: params[:new_email]) )
                        old_mail = user.email
                        set_email(user)
                        UserMailer.email_changed(old_mail, user).deliver
                        render json: { 
                            response: "success",
                            result: "Email changed"
                            }, status: :ok
                    else
                        render json: { 
                            response: "failure",
                            error: "Email already used"
                            }, status: :bad_request
                    end
                else
                    render json: { 
                        response: "failure",
                        error: "User created with oAuth"
                        }, status: :unauthorized
                end
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
        if user 
            if not user.oauth
                if user and user.reset_pass
                    set_pass(user)
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
            else
                render json: { 
                    response: "failure",
                    error: "User created with oAuth"
                    }, status: :unauthorized
            end
        else
            render json: { 
                response: "failure",
                error: "User not found" 
                }, status: :bad_request
        end
    end

    def change_pass
        check = AuthorizeApiRequest.call(request.headers)
        if check.success?
            user = User.find(check.result.id)
            if user 
                if user.oauth == nil or not user.oauth
                    if user.authenticate(params[:old_pass])
                        set_pass(user)
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
                        error: "User created with oAuth"
                        }, status: :unauthorized
                end
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


    private
    
    def set_email(user)
        user.email = params[:new_email]         if not (params[:new_email].nil? or params[:new_email].empty?) 
        user.password = params[:new_pass]       if not (params[:new_email].nil? or params[:new_email].empty?)
        
        user.save
    end

    def set_pass(user)
        user.password = params[:new_pass]       if not (params[:new_pass].nil? or params[:new_pass].empty?)
        user.temp_pass = nil                    if not (params[:new_pass].nil? or params[:new_pass].empty?)
        user.reset_pass = false                 if not (params[:new_pass].nil? or params[:new_pass].empty?)
        
        user.save
    end

end