class RegisterUser
    prepend SimpleCommand

    def initialize(email, password)
        @email = email
        @password = password
    end

    def call
        @user = User.create(email: @email , password: @password , password_confirmation: @password) if signup_check
        @user = User.find_by_email(@email)
        create_token(@user)
        UserMailer.registration_confirmation(@user).deliver
    end

    private

    attr_accessor :username, :email, :password

    def signup_check
        exist_email = User.exists?(email: @email)

        if exist_email
            errors.add :user_registration, 'email'
            return false
        else
            return true
        end
        
    end

    def create_token(user)
        if user.confirm_token.blank? or user.confirm_token.nil?
            user.confirm_token = SecureRandom.urlsafe_base64.to_s
            user.save
        end
    end
end