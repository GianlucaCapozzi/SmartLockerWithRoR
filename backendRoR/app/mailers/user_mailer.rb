class UserMailer < ActionMailer::Base
    default :from => "confirmation@smartlocker.com"

    public

    def registration_confirmation(user)
        @user = user
        @url = "https://smart-locker-macc.herokuapp.com/activation/"+@user.confirm_token
        mail(:to => @user.email, :subject => "Registration Confirmation", :template_name => "registration_confirmation")
    end

    def recovery_password(user, temp_pass)
        @user = user
        @temp_pass = temp_pass
        mail(:to => @user.email, :subject => "Recovery Password", :template_name => "recovery_password")
    end

end